package egovframework.portal.video.service;

import com.github.pagehelper.PageInfo;
import egovframework.portal.video.dto.VideoDTO;
import egovframework.portal.video.dto.VideoFilter;

public interface VideoService {
    /**
     * 비디오 목록 조회 (페이징 처리)
     */
    PageInfo<?> selectVideoPostListWithFilter(VideoFilter filter);

    /**
     * 비디오 상세 조회
     */
    VideoDTO selectVideoById(Long videoNo) ;
}