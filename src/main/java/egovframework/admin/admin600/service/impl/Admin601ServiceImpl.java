package egovframework.admin.admin600.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin600.domain.Admin601DetailDTO;
import egovframework.admin.admin600.domain.Admin601SaveDTO;
import egovframework.admin.admin600.domain.Admin601VO;
import egovframework.admin.admin600.mapper.Admin601Mapper;
import egovframework.admin.admin600.service.Admin601Service;
import egovframework.common.content.ContentProcessService;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.file.domain.FileDTO;
import egovframework.common.file.mapper.FileMapper;
import egovframework.common.file.service.FileService;
import egovframework.common.util.CommonUtil;
import egovframework.common.util.HtmlUtil;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Admin601ServiceImpl extends EgovAbstractServiceImpl implements Admin601Service {
    private final Admin601Mapper admin601Mapper;
    private final FileService fileService;
    private final FileMapper fileMapper;
    private final ContentProcessService contentProcessService;

    private static final String TABLE_NOTICE = "ntc";

    @Override
    public PageInfo<Admin601VO> selectNoticeList(EgovMap egovMap) {
        int pageNum = Integer.parseInt(String.valueOf(egovMap.get("pageNum") == null ? 1 : egovMap.get("pageNum")));
        int pageSize = Integer.parseInt(String.valueOf(egovMap.get("pageSize") == null ? 10 : egovMap.get("pageSize")));

        // PageHelper를 사용하여 페이징 처리
        PageHelper.startPage(pageNum, pageSize);
        List<Admin601VO> list = admin601Mapper.selectNoticeList(egovMap);
        return new PageInfo<>(list);
    }

    @Override
    public Admin601DetailDTO selectNotice(Long ntcOid) {
        Admin601VO notice = admin601Mapper.selectNotice(ntcOid);

        // 파일 테이블에서 조회할 파일의 테이블명, 해당 테이블의 PK 설정
        EgovMap egovMap = new EgovMap();
        egovMap.put("tblNm", TABLE_NOTICE);
        egovMap.put("tblOid", ntcOid);

        // 첨부 파일 조회
        List<FileDTO> noticeAttachList = fileMapper.selectAttachmentFileByTableNameAndTablePk(egovMap);
        // ckeditor 파일 조회
        List<FileDTO> noticeCkEditorAttachList = fileMapper.selectInlineFileByTableNameAndTablePk(egovMap);

        Admin601DetailDTO detailDTO = new Admin601DetailDTO();
        // 공지사항 상세
        detailDTO.setNtcOid(ntcOid);
        detailDTO.setNtcNm(notice.getNtcNm());
        detailDTO.setNtcCn(notice.getNtcCn());
        detailDTO.setBgngDt(notice.getBgngDt());
        detailDTO.setEndDt(notice.getEndDt());
        detailDTO.setUpendFixYn(notice.getUpendFixYn());
        detailDTO.setUpendFixBgngDt(notice.getUpendFixBgngDt());
        detailDTO.setUpendFixEndDt(notice.getUpendFixEndDt());
        detailDTO.setOpenYn(notice.getOpenYn());
        detailDTO.setUseYn(notice.getUseYn());
        detailDTO.setInqCnt(notice.getInqCnt());
        detailDTO.setRegId(notice.getRegId());
        detailDTO.setRegDt(notice.getRegDt());
        detailDTO.setMdfcnId(notice.getMdfcnId());
        detailDTO.setMdfcnDt(notice.getMdfcnDt());
        // 공지사항 상세의 첨부 파일 목록
        detailDTO.setNoticeAttachList(noticeAttachList);
        // 공지사항 상세의 ckeditor 파일 목록
        detailDTO.setNoticeCkEditorAttachList(noticeCkEditorAttachList);
        return detailDTO;
    }

    @Transactional
    @Override
    public int insertNotice(EgovMap egovMap) throws RuntimeException, IOException {
        int result = 0;
        Admin601SaveDTO dto = (Admin601SaveDTO) egovMap.get("admin601SaveDTO");
        @SuppressWarnings("unchecked")
        List<MultipartFile> uploadFiles = (List<MultipartFile>) egovMap.get("uploadFiles");
        if (dto == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "요청 데이터가 올바르지 않습니다.");
        }

        // contents_text 생성
        dto.setNtcCnTxt(HtmlUtil.stripHtml(dto.getNtcCn()));

        egovMap.clear();
        Admin601SaveDTO.populateDtoFields(egovMap, dto);
        egovMap.put("path", "notice"); // 공통 파일 경로 지정
        egovMap.put("uploadFiles", uploadFiles);
        egovMap.put("attachedFiles", dto.getAttachedFiles());
        egovMap.put("editorFiles", dto.getEditorFiles());
        egovMap.put("content", dto.getNtcCn());

        String userId = SecurityUtil.getUser().getUserId();
        egovMap.put("regId", userId);
        egovMap.put("mdfcnId", userId);
        // 1) 파일/본문 정규화 (임시 → 실제 경로 이동, CKEditor 본문 치환)
        fileService.processFiles(egovMap);

		egovMap.put("content", contentProcessService.processHtmlContent((String) egovMap.get("content")));

        // 2) 공지 저장 (selectKey로 ntcOid 세팅됨)
        result = admin601Mapper.insertNotice(egovMap);

        if (result == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "공지사항 저장 에러");
        }

        // 3) file 메타 기록: 방금 생성된 ntcOid를 tablePk로 연결
        Long ntcOid = CommonUtil.toLong(egovMap.get("ntcOid"));
        egovMap.put("tblNm", TABLE_NOTICE);
        egovMap.put("tblOid", ntcOid);
        fileService.saveFileMeta(egovMap);

        // 4) 첫 번째 첨부파일의 alt_text 업데이트
        if (StringUtils.hasText(dto.getFirstFileAltText())) {
            updateFirstFileImgSbstTxtCn(ntcOid, dto.getFirstFileAltText(), userId);
        }

        return result;
    }

    @Transactional
    @Override
    public int updateNotice(EgovMap egovMap) throws RuntimeException, IOException {
        Admin601SaveDTO dto = (Admin601SaveDTO) egovMap.get("admin601SaveDTO");
        @SuppressWarnings("unchecked")
        List<MultipartFile> uploadFiles = (List<MultipartFile>) egovMap.get("uploadFiles");
        if (dto == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "요청 데이터가 올바르지 않습니다.");
        }

        // contents_text 생성
        dto.setNtcCnTxt(HtmlUtil.stripHtml(dto.getNtcCn()));

        egovMap.clear();
        Admin601SaveDTO.populateDtoFields(egovMap, dto);
        egovMap.put("path", "notice");
        egovMap.put("uploadFiles", uploadFiles);
        egovMap.put("attachedFiles", dto.getAttachedFiles());
        egovMap.put("editorFiles", dto.getEditorFiles());
        egovMap.put("tblNm", TABLE_NOTICE);
        egovMap.put("tblOid", dto.getNtcOid());

		String userId = SecurityUtil.getUser().getUserId();
		egovMap.put("mdfcnId", userId);
		// 파일/본문 정규화
		fileService.processFiles(egovMap);

		egovMap.put("content", contentProcessService.processHtmlContent((String) egovMap.get("content")));

		// file 메타 반영 (tablePk는 DTO에서 전달된 ntcOid 사용)
		fileService.saveFileMeta(egovMap);

        // 첫 번째 첨부파일의 alt_text 업데이트
        if (StringUtils.hasText(dto.getFirstFileAltText())) {
            updateFirstFileImgSbstTxtCn(dto.getNtcOid(), dto.getFirstFileAltText(), userId);
        }

        int result = admin601Mapper.updateNotice(egovMap);

        if (result == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "공지사항 저장 에러");
        }

		@SuppressWarnings("unchecked")
		List<Long> deleteAttachNos = (List<Long>) egovMap.get("deleteAttachNos"); // file.file_no로 사용
		@SuppressWarnings("unchecked")
		List<Long> deleteEditorAttachNos = (List<Long>) egovMap.get("deleteEditorAttachNos"); // 동일하게 file_no로 사용

        if (!CollectionUtils.isEmpty(deleteAttachNos)) {
            fileService.deleteFilesByFileNos(deleteAttachNos, userId);
        }
        if (!CollectionUtils.isEmpty(deleteEditorAttachNos)) {
            fileService.deleteFilesByFileNos(deleteEditorAttachNos, userId);
        }

        return result;
    }

    @Transactional
    @Override
    public int deleteNotice(Long ntcOid) {
        int deleteNoticeResult = 0;

        String userId = SecurityUtil.getUser().getUserId();

        EgovMap egovMap = new EgovMap();
        egovMap.put("ntcOid", ntcOid);
        egovMap.put("mdfcnId", userId);

		// file 메타 논리 삭제 (물리 삭제는 파일 테이블 일원화 후 후속 처리 가능)
		fileService.deleteFilesByTable(TABLE_NOTICE, ntcOid, userId);

//		deleteNoticeAttachResult = admin601Mapper.deleteNoticeAttachByNtcOid(egovMap);
        deleteNoticeResult = admin601Mapper.deleteNotice(egovMap);

        // 공지사항 삭제 실패 했을 경우, 공지사항에 이미지가 존재 하는데 그 이미지 삭제를 실패한 경우
        if (deleteNoticeResult == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "공지사항 삭제 에러");
        }

        return deleteNoticeResult;
    }

    @Transactional
    @Override
    public void updateNoticeInqCnt(Long ntcOid) {
        if (ntcOid == null) {
            return;
        }
        admin601Mapper.updateNoticeInqCnt(ntcOid);
    }

    /**
     * 첫 번째 첨부파일의 alt_text 업데이트
     */
    private void updateFirstFileImgSbstTxtCn(Long ntcOid, String imgSbstTxtCn, String userId) {
		if (ntcOid == null || !StringUtils.hasText(imgSbstTxtCn)) {
			return;
		}

		// 첫 번째 첨부파일 조회 (attach_order = 1, inline_yn = 'N')
		EgovMap egovMap = new EgovMap();
		egovMap.put("tblNm", TABLE_NOTICE);
		egovMap.put("tblOid", ntcOid);
		egovMap.put("inlineYn", "N");
		egovMap.put("atchFileSeq", 1);

		List<FileDTO> files = fileMapper.selectAttachmentFileByTableNameAndTablePk(egovMap);

		if (files != null && !files.isEmpty()) {
			FileDTO firstFile = files.get(0);
			log.info("첫 번째 첨부파일 alt_text 업데이트 - fileOid: {}, imgSbstTxtCn: {}", firstFile.getFileOid(), imgSbstTxtCn);

			EgovMap updateMap = new EgovMap();
			updateMap.put("fileOid", firstFile.getFileOid());
			updateMap.put("imgSbstTxtCn", imgSbstTxtCn);
			updateMap.put("mdfcnId", userId);

			fileMapper.updateFileAltText(updateMap);
		}
	}
}