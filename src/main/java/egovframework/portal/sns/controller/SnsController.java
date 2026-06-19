package egovframework.portal.sns.controller;

import egovframework.common.api.ApiResponse;
import egovframework.portal.sns.dto.SnsDTO;
import egovframework.portal.sns.dto.YoutubeDTO;
import egovframework.portal.sns.service.SnsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/portal/sns")
public class SnsController {
    private final SnsService snsService;

    /**
     * Sns 목록 조회
     *
     * @return ResponseEntity<ApiResponse<List<SnsDTO>>>
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<SnsDTO>>> selectSnsList() {
        List<SnsDTO> snsList = snsService.selectSnsList();
        return ResponseEntity.ok(ApiResponse.success(snsList));
    }

    /**
     * YouTube 최근 영상 조회
     */
    @GetMapping("/youtube/videos")
    public ResponseEntity<ApiResponse<List<YoutubeDTO>>> getYoutubeVideos(
            @RequestParam(required = false) String snsChnlOid,
            @RequestParam(defaultValue = "4") int limit) {

        List<YoutubeDTO> videos = snsService.getYoutubeVideos(snsChnlOid, limit);
        return ResponseEntity.ok(ApiResponse.success(videos));
    }
}