package egovframework.portal.video.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.portal.video.dto.VideoDTO;
import egovframework.portal.video.dto.VideoFilter;
import egovframework.portal.video.mapper.VideoMapper;
import egovframework.portal.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.jsoup.internal.StringUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl extends EgovAbstractServiceImpl implements VideoService {
    private final VideoMapper videoMapper;

    @Override
    public PageInfo<?> selectVideoPostListWithFilter(VideoFilter filter) {
        if (filter != null && filter.getPage() != null && filter.getSize() != null) {
            PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
        }
        List<?> videoPostList = videoMapper.selectVideoPostListWithFilter(filter);

        return new PageInfo<>(videoPostList);
    }

    @Override
    public VideoDTO selectVideoById(Long videoNo) {
        if (videoNo == null) {
            throw new IllegalArgumentException("비디오 ID는 필수입니다.");
        }
        VideoDTO videoPost = videoMapper.selectVideoById(videoNo);

        // 이전글/다음글 조회
        VideoDTO prevVideo = videoMapper.selectPrevVideo(videoNo);
        VideoDTO nextVideo = videoMapper.selectNextVideo(videoNo);

        // 유튜브 embed URL
        String embedUrl = convertToEmbedUrl(videoPost.getVdoUrl());

        videoPost.setPrevVideo(prevVideo);
        videoPost.setNextVideo(nextVideo);
        videoPost.setEmbedUrl(embedUrl);

        return videoPost;
    }

    /*유튜브 공유 링크를 embed 링크로 변환*/
    private String convertToEmbedUrl(String youtubeUrl) {
        if (StringUtil.isBlank(youtubeUrl)) {
            return "";
        }

        try {
            String videoId = "";
            // 이미 embed URL인 경우
            if (youtubeUrl.contains("youtube.com/embed/")) {
                videoId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1);
                if (videoId.contains("?")) {
                    videoId = videoId.substring(0, videoId.indexOf("?"));
                }
            }
            // 공유용 URL인 경우 (youtu.be/ 형식)
            else if (youtubeUrl.contains("youtu.be/")) {
                videoId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1);
            }
            // YouTube Shorts URL인 경우
            else if (youtubeUrl.contains("youtube.com/shorts/")) {
                videoId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1);
            }
            // 일반 URL인 경우 (v= 파라미터 사용)
            else if (youtubeUrl.contains("v=")) {
                videoId = youtubeUrl.substring(youtubeUrl.indexOf("v=") + 2);
                if (videoId.contains("&")) {
                    videoId = videoId.substring(0, videoId.indexOf("&"));
                }
            }

            return !StringUtil.isBlank(videoId) ? "https://www.youtube.com/embed/" + videoId : "";
        } catch (Exception e) {
            return null;
        }
    }
}
