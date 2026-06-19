package egovframework.portal.video.controller;

import com.github.pagehelper.PageInfo;
import egovframework.common.api.ApiResponse;
import egovframework.portal.video.dto.VideoFilter;
import egovframework.portal.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/portal/video")
public class VideoController {
    private final VideoService videoService;

    /**
     * Video 게시판 게시글 목록 조회
     *
     * @param filter VideoFilter
     * @return ResponseEntity<ApiResponse<PageInfo<?>>>
     */
    @GetMapping("/list/filter")
    public ResponseEntity<ApiResponse<PageInfo<?>>> selectVideoPostListWithFilter(@ModelAttribute VideoFilter filter) {
        PageInfo<?> videoPostList = videoService.selectVideoPostListWithFilter(filter);
        return ResponseEntity.ok(ApiResponse.success(videoPostList));
    }
}