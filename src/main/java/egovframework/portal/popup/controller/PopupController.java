package egovframework.portal.popup.controller;

import egovframework.common.api.ApiResponse;
import egovframework.portal.popup.dto.PopupDTO;
import egovframework.portal.popup.service.PopupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/portal/popup")
public class PopupController {
    private final PopupService popupService;

    /**
     * Popup 목록 조회
     *
     * @return ResponseEntity<ApiResponse<List<PopupDTO>>>
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<PopupDTO>>> selectPopupList() {
        List<PopupDTO> popupList = popupService.selectPopupList();
        return ResponseEntity.ok(ApiResponse.success(popupList));
    }
}