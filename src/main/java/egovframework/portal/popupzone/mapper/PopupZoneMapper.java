package egovframework.portal.popupzone.mapper;

import egovframework.portal.popupzone.dto.PopupZoneDTO;
import egovframework.portal.popupzone.dto.PopupZoneFilter;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface PopupZoneMapper {

    /**
     * popupZone 게시판 게시글 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return List<PopupZoneDTO> 게시글 목록
     */
    List<PopupZoneDTO> selectPopupZonePostListWithFilter(PopupZoneFilter filter);

    /**
     * popupZone 게시판 게시글 단건 조회
     * @param id 검색조건 (popupZone id)
     * @return PopupZoneDTO 게시글
     */
    PopupZoneDTO selectPopupZoneById(Long id);

    /**
     * popupZone 게시판 이전 게시글 조회
     * @param id 검색조건 (popupZone id)
     * @return PopupZoneDTO 이전 게시글
     */
    PopupZoneDTO selectPrevPopupZone(Long id);

    /**
     * popupZone 게시판 다음 게시글 조회
     * @param id 검색조건 (popupZone id)
     * @return PopupZoneDTO 다음 게시글
     */
    PopupZoneDTO selectNextPopupZone(Long id);
}