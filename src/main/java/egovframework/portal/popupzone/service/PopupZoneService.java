package egovframework.portal.popupzone.service;

import com.github.pagehelper.PageInfo;
import egovframework.portal.cardnews.dto.CardnewsDTO;
import egovframework.portal.cardnews.dto.CardnewsFilter;
import egovframework.portal.popupzone.dto.PopupZoneDTO;
import egovframework.portal.popupzone.dto.PopupZoneFilter;

public interface PopupZoneService {
    /**
     * popupZone 목록 조회 (페이징 처리)
     */
    PageInfo<?> selectPopupZonePostListWithFilter(PopupZoneFilter filter);

    /**
     * popupZone 상세 조회
     */
    PopupZoneDTO selectPopupZoneById(Long popupZoneOid) ;
}