package egovframework.portal.notice.controller;

import com.github.pagehelper.PageInfo;
import egovframework.common.api.ApiResponse;
import egovframework.portal.notice.dto.NoticeDTO;
import egovframework.portal.notice.dto.NoticeFilter;
import egovframework.portal.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/notice")
public class NoticeController {
    private final NoticeService noticeService;

    /**
     * Notice 게시판 게시글 목록 조회
     *
     * @param filter NoticeFilter
     * @return ResponseEntity<ApiResponse<PageInfo<?>>>
     */
    @GetMapping("/list/filter")
    public ResponseEntity<ApiResponse<PageInfo<?>>> selectNoticePostListWithFilter(@ModelAttribute NoticeFilter filter) {
        PageInfo<?> noticePostList = noticeService.selectNoticePostListWithFilter(filter);
        return ResponseEntity.ok(ApiResponse.success(noticePostList));
    }

    /**
     * Notice 게시판 조회수 증가
     *
     * @param noticeDTO NoticeFilter
     * @return ResponseEntity<ApiResponse<T>>
     */
    @PatchMapping("/update/inq-cnt")
    public ResponseEntity<ApiResponse<T>> updateNoticeInqCnt(@RequestBody NoticeDTO noticeDTO) {
        noticeService.updateNoticeInqCnt(noticeDTO);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
    }
}
