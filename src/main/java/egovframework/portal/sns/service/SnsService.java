package egovframework.portal.sns.service;

import egovframework.portal.sns.dto.SnsDTO;
import egovframework.portal.sns.dto.YoutubeDTO;

import java.util.List;

public interface SnsService {

    /**
     * Sns 목록 조회
     */
    List<SnsDTO> selectSnsList();

    /**
     * Youtube 동영상 조회 (snsChnlOid에 따라 특정 채널 또는 전체 YOUTUBE 채널)
     */
    List<YoutubeDTO> getYoutubeVideos(String snsChnlOid, int limit);

    /**
     * Youtube 동영상 조회 (RSS URL 기반)
     */
    List<YoutubeDTO> selectYoutubeVideos(String rssUrl, int limit);

    /**
     * SNS 목록 캐시 무효화 (관리자 페이지에서 SNS 데이터 변경 시 호출)
     */
    void evictSnsCache();

    /**
     * 모든 YouTube 채널의 RSS 캐시를 갱신
     */
    void refreshAllYoutubeCache();
}
