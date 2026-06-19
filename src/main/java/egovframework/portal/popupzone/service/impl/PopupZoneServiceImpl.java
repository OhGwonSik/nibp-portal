package egovframework.portal.popupzone.service.impl;

import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import egovframework.common.file.domain.FileDTO;
import egovframework.common.file.mapper.FileMapper;
import egovframework.portal.popupzone.dto.PopupZoneDTO;
import egovframework.portal.popupzone.dto.PopupZoneFilter;
import egovframework.portal.popupzone.mapper.PopupZoneMapper;
import egovframework.portal.popupzone.service.PopupZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopupZoneServiceImpl extends EgovAbstractServiceImpl implements PopupZoneService {
    private final PopupZoneMapper popupZoneMapper;
    private final FileMapper fileMapper;

    @Override
    public PageInfo<?> selectPopupZonePostListWithFilter(PopupZoneFilter filter) {
        if (filter != null && filter.getPage() != null && filter.getSize() != null) {
            PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
        }
        List<?> popupZonePostList = popupZoneMapper.selectPopupZonePostListWithFilter(filter);

        return new PageInfo<>(popupZonePostList);
    }

    @Override
    public PopupZoneDTO selectPopupZoneById(Long popupZoneOid) {
        if (popupZoneOid == null) {
            throw new IllegalArgumentException("팝업존 ID는 필수입니다.");
        }
        PopupZoneDTO popupZonePost = popupZoneMapper.selectPopupZoneById(popupZoneOid);

        // 해당 팝업존 첨부 파일 목록 조회 (file 테이블에서)
        EgovMap egovMap = new EgovMap();
        egovMap.put("tblNm", "popup_zone");     // 어떤 테이블의 첨부 파일인지
        egovMap.put("tblOid", popupZoneOid);           // 어떤 테이블의 어떤 pk의 첨부 파일인지
        List<FileDTO> attachFileList = fileMapper.selectAttachmentFileByTableNameAndTablePk(egovMap);

        // 해당 팝업존 첨부 파일 목록 DTO에 추가
        popupZonePost.setAttachments(attachFileList);

        // 이전글/다음글 조회
        PopupZoneDTO prevPopupZone = popupZoneMapper.selectPrevPopupZone(popupZoneOid);
        PopupZoneDTO nextPopupZone = popupZoneMapper.selectNextPopupZone(popupZoneOid);

        popupZonePost.setPrevPopupZone(prevPopupZone);
        popupZonePost.setNextPopupZone(nextPopupZone);

        return popupZonePost;
    }
}
