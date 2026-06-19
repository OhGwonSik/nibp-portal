package egovframework.portal.faq.controller;

import com.github.pagehelper.PageInfo;
import egovframework.common.api.ApiResponse;
import egovframework.portal.faq.dto.FaqFilter;
import egovframework.portal.faq.service.FaqService;
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
@RequestMapping("/api/common/portal/faq")
public class FaqController {
    private final FaqService faqService;

    /**
     * Faq 게시판 게시글 목록 조회
     *
     * @param filter FaqFilter
     * @return ResponseEntity<ApiResponse<PageInfo<Admin614VO>>>
     */
    @GetMapping("/list/filter")
    public ResponseEntity<ApiResponse<PageInfo<?>>> selectFaqPostListWithFilter(@ModelAttribute FaqFilter filter) {
        PageInfo<?> faqPostList = faqService.selectFaqPostListWithFilter(filter);
        return ResponseEntity.ok(ApiResponse.success(faqPostList));
    }
}