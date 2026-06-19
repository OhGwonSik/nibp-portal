package egovframework.portal.popup.service;

import egovframework.portal.popup.dto.PopupDTO;

import java.util.List;

public interface PopupService {

    /**
     * Popup 목록 조회
     */
    List<PopupDTO> selectPopupList();

    /**
     * Popup 캐시 무효화 (관리자 페이지에서 팝업 변경 시 호출)
     */
    void evictPopupCache();
}