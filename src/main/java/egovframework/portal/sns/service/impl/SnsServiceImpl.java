package egovframework.portal.sns.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.portal.sns.dto.SnsDTO;
import egovframework.portal.sns.dto.YoutubeDTO;
import egovframework.portal.sns.mapper.SnsMapper;
import egovframework.portal.sns.service.SnsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnsServiceImpl extends EgovAbstractServiceImpl implements SnsService {
    private final SnsMapper snsMapper;
    private static final String SNS_LIST_CACHE_KEY = "snsList";

    @Value("${app.cache.youtube.expire-minutes:60}")
    private int youtubeCacheExpirationMinutes;

    @Value("${app.cache.sns.expire-minutes:60}")
    private int snsCacheExpirationMinutes;

    private Cache<String, List<YoutubeDTO>> youtubeCache;
    private Cache<String, List<SnsDTO>> snsCache;
    private final ConcurrentHashMap<String, List<YoutubeDTO>> lastKnownGood = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        youtubeCache = Caffeine.newBuilder()
            .expireAfterWrite(youtubeCacheExpirationMinutes, TimeUnit.MINUTES)
            .build();
        snsCache = Caffeine.newBuilder()
            .expireAfterWrite(snsCacheExpirationMinutes, TimeUnit.MINUTES)
            .build();
    }

    @Override
    public List<SnsDTO> selectSnsList() {
        List<SnsDTO> cachedList = snsCache.getIfPresent(SNS_LIST_CACHE_KEY);
        if (cachedList != null) {
            log.debug("Serving SNS list from cache");
            return cachedList;
        }

        log.debug("Fetching SNS list from database");
        List<SnsDTO> snsList = snsMapper.selectSnsList();
        snsCache.put(SNS_LIST_CACHE_KEY, snsList);
        return snsList;
    }

    // 캐시 무효화 메소드(추후 관리자페이지 생성시 사용)
    @Override
    public void evictSnsCache() {
        snsCache.invalidate(SNS_LIST_CACHE_KEY);
        log.info("SNS list cache evicted for key: {}", SNS_LIST_CACHE_KEY);
    }

    @Override
    public List<YoutubeDTO> getYoutubeVideos(String snsChnlOid, int limit) {
        List<SnsDTO> snsList = selectSnsList(); // 캐시된 SNS 목록 사용
        List<YoutubeDTO> allVideos = new ArrayList<>();

        if (snsChnlOid != null && !snsChnlOid.isEmpty()) {
            // 특정 채널 ID가 주어진 경우
            String rssUrl = snsList.stream()
                .filter(sns -> sns.getSnsChnlOid().equals(Long.parseLong(snsChnlOid)))
                .findFirst()
                .map(SnsDTO::getRssFeedUrl)
                .orElse(null);

            if (rssUrl == null) {
                return new ArrayList<>();
            }

            return selectYoutubeVideos(rssUrl, limit);
        } else {
            // snsChnlOid가 없으면 PLATFORM_TYPE이 YOUTUBE인 모든 채널에서 영상 가져오기
            List<SnsDTO> youtubeChannels = snsList.stream()
                .filter(sns -> "YOUTUBE".equalsIgnoreCase(sns.getPltfmType()))
                .toList();

            // 각 채널에서 6개씩 영상 가져오기
            int videosPerChannel = 6;
            for (SnsDTO channel : youtubeChannels) {
                if (channel.getRssFeedUrl() != null && !channel.getRssFeedUrl().isEmpty()) {
                    List<YoutubeDTO> videos = selectYoutubeVideos(channel.getRssFeedUrl(), videosPerChannel);

                    // 각 비디오에 채널명 설정
                    for (YoutubeDTO video : videos) {
                        video.setChannelName(channel.getChnlNm());
                    }

                    allVideos.addAll(videos);
                }
            }

            return allVideos;
        }
    }

    @Override
    public List<YoutubeDTO> selectYoutubeVideos(String rssUrl, int limit) {
        List<YoutubeDTO> cachedVideos = youtubeCache.getIfPresent(rssUrl);

        if (cachedVideos != null) {
            log.info("Serving YouTube RSS feed from cache for URL: {}", rssUrl);
            return cachedVideos.stream()
                .limit(limit)
                .toList();
        }

        try {
            List<YoutubeDTO> allVideos = youtubeCache.get(rssUrl, k -> fetchAndParseYoutubeRss(k));

            if (allVideos == null || allVideos.isEmpty()) {
                return Collections.emptyList();
            }

            return allVideos.stream()
                .limit(limit)
                .toList();

        } catch (Exception e) {
            log.error("Failed to get YouTube videos for URL: {}. Reason: {}", rssUrl, e.getMessage());

            List<YoutubeDTO> fallback = lastKnownGood.get(rssUrl);
            if (fallback != null) {
                log.warn("YouTube 차단됨. lastKnownGood 캐시에서 반환. URL: {}", rssUrl);
                return fallback.stream().limit(limit).toList();
            }

            if (e.getCause() instanceof BusinessException) {
                throw (BusinessException) e.getCause();
            }
            return Collections.emptyList();
        }
    }

    private List<YoutubeDTO> fetchAndParseYoutubeRss(String rssUrl) {
        log.info("Fetching YouTube RSS feed from URL: {}", rssUrl);
        List<YoutubeDTO> videos = new ArrayList<>();
        try {
            URL feedUrl = new URL(rssUrl);
            HttpURLConnection connection = (HttpURLConnection) feedUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(connection));

            feed.getEntries().forEach(entry -> {
                String link = entry.getLink();
                String videoId = extractVideoId(link);
                String thumbnail = getAvailableThumbnail(videoId);
                videos.add(YoutubeDTO.builder()
                        .videoId(videoId)
                        .title(entry.getTitle())
                        .link(link)
                        .thumbnail(thumbnail)
                        .build());
            });

            if (!videos.isEmpty()) {
                lastKnownGood.put(rssUrl, List.copyOf(videos));
            }
            return videos;
        } catch (FileNotFoundException e) {
            log.error("유튜브 채널을 찾을 수 없거나 접근이 차단되었습니다. URL: {}", rssUrl);
            throw new BusinessException(ErrorCode.EXTERNAL_SERVICE_ERROR, "유튜브 채널을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("YouTube RSS 파싱 에러 - URL: {}, Message: {}", rssUrl, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "YouTube RSS 파싱 에러 발생");
        }
    }

    /**
     * YouTube URL에서 비디오 ID 추출
     * - 일반 영상: https://www.youtube.com/watch?v=VIDEO_ID
     * - 쇼츠: https://www.youtube.com/shorts/VIDEO_ID
     */
    private String extractVideoId(String link) {
        if (link == null || link.isEmpty()) {
            return "";
        }

        // 쇼츠 URL 처리: /shorts/VIDEO_ID
        if (link.contains("/shorts/")) {
            int shortsIndex = link.indexOf("/shorts/") + 8;
            String videoId = link.substring(shortsIndex);

            // 쿼리 파라미터나 추가 경로가 있을 경우 제거
            int queryIndex = videoId.indexOf("?");
            if (queryIndex != -1) {
                videoId = videoId.substring(0, queryIndex);
            }
            int slashIndex = videoId.indexOf("/");
            if (slashIndex != -1) {
                videoId = videoId.substring(0, slashIndex);
            }

            return videoId;
        }

        // 일반 영상 URL 처리: ?v=VIDEO_ID
        if (link.contains("v=")) {
            String videoId = link.substring(link.indexOf("v=") + 2);

            // & 이후 다른 파라미터가 있을 경우 제거
            int ampersandIndex = videoId.indexOf("&");
            if (ampersandIndex != -1) {
                videoId = videoId.substring(0, ampersandIndex);
            }

            return videoId;
        }

        return "";
    }

    /**
     * 사용 가능한 YouTube 썸네일 URL 반환
     * 우선순위: hqdefault > mqdefault > default
     * 모두 실패 시 빈 문자열 반환
     */
    @Override
    public void refreshAllYoutubeCache() {
        log.info("YouTube 캐시 갱신 시작");
        List<SnsDTO> youtubeChannels = selectSnsList().stream()
            .filter(sns -> "YOUTUBE".equalsIgnoreCase(sns.getPltfmType()))
            .filter(sns -> sns.getRssFeedUrl() != null && !sns.getRssFeedUrl().isEmpty())
            .toList();

        for (SnsDTO channel : youtubeChannels) {
            try {
                selectYoutubeVideos(channel.getRssFeedUrl(), 6);
                log.info("YouTube 캐시 갱신 완료: {}", channel.getChnlNm());
            } catch (Exception e) {
                log.warn("YouTube 캐시 갱신 실패: {} - {}", channel.getChnlNm(), e.getMessage());
            }
        }
        log.info("YouTube 캐시 갱신 종료. 총 {}개 채널 처리", youtubeChannels.size());
    }

    private String getAvailableThumbnail(String videoId) {
        if (videoId == null || videoId.isEmpty()) {
            return "";
        }

        return "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
    }
}
