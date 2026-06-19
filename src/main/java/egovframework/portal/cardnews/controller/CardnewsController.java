package egovframework.portal.cardnews.controller;

import com.github.pagehelper.PageInfo;
import egovframework.common.api.ApiResponse;
import egovframework.portal.cardnews.dto.CardnewsFilter;
import egovframework.portal.cardnews.service.CardnewsService;
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
@RequestMapping("/api/common/portal/cardnews")
public class CardnewsController {
    private final CardnewsService cardnewsService;

    /**
     * Cardnews 게시판 게시글 목록 조회
     *
     * @param filter PopupZoneFilter
     * @return ResponseEntity<ApiResponse<PageInfo<?>>>
     */
    @GetMapping("/list/filter")
    public ResponseEntity<ApiResponse<PageInfo<?>>> selectCardnewsPostListWithFilter(@ModelAttribute CardnewsFilter filter) {
        log.info("Searching cardnews list with filter: {}", filter);
        PageInfo<?> cardnewsPostList = cardnewsService.selectCardnewsPostListWithFilter(filter);
        return ResponseEntity.ok(ApiResponse.success(cardnewsPostList));
    }
}