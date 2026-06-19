package egovframework.portal.popupzone.controller;

import com.github.pagehelper.PageInfo;
import egovframework.common.api.ApiResponse;
import egovframework.portal.cardnews.service.CardnewsService;
import egovframework.portal.popupzone.dto.PopupZoneFilter;
import egovframework.portal.popupzone.service.PopupZoneService;
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
@RequestMapping("/api/common/portal/popup-zone")
public class PopupZoneController {
    private final PopupZoneService popupZoneService;

    /**
     * PopupZone 게시판 게시글 목록 조회
     *
     * @param filter PopupZoneFilter
     * @return ResponseEntity<ApiResponse<PageInfo<?>>>
     */
    @GetMapping("/list/filter")
    public ResponseEntity<ApiResponse<PageInfo<?>>> selectPopupZonePostListWithFilter(@ModelAttribute PopupZoneFilter filter) {
        log.info("Searching popupZone list with filter: {}", filter);
        PageInfo<?> popupZonePostList = popupZoneService.selectPopupZonePostListWithFilter(filter);
        return ResponseEntity.ok(ApiResponse.success(popupZonePostList));
    }
}