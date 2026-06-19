package egovframework.admin.admin600.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin600.domain.*;
import egovframework.admin.admin600.mapper.Admin614Mapper;
import egovframework.admin.admin600.service.Admin614Service;
import egovframework.common.content.ContentProcessService;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.file.domain.FileDTO;
import egovframework.common.file.mapper.FileMapper;
import egovframework.common.file.service.FileService;
import egovframework.common.util.HtmlUtil;
import egovframework.common.util.SecurityUtil;
import egovframework.portal.popup.service.PopupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @ClassName : Admin614ServiceImpl.java
 * @Description : 안내(팝업) 관리 서비스 구현체
 *
 * @author : balee
 * @since  : 2025. 11. 24
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Admin614ServiceImpl extends EgovAbstractServiceImpl implements Admin614Service {
    private final Admin614Mapper admin614Mapper;
    private final ExcelComponent excelComponent;
    private final ExcelConfig excelConfig;
    private final FileService fileService;
    private final FileMapper fileMapper;
    private final ContentProcessService contentProcessService;
    private final PopupService popupService;

    private static final String TABLE_POPUP = "popup";

	@Override
	public PageInfo<Admin614VO> selectAdmin614List(Admin614FilterDTO filter) {
		PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());

		List<Admin614VO> admin614List = admin614Mapper.selectAdmin614List(filter);

		return new PageInfo<>(admin614List);
	}

    @Override
    public Admin614VO selectAdmin614(String popupOid) {
        Admin614VO admin614 = admin614Mapper.selectAdmin614(popupOid);
        if (admin614 == null) {
            return null;
        }

        // 파일 테이블에서 조회할 파일의 테이블명, 해당 테이블의 PK 설정
        EgovMap egovMap = new EgovMap();
        egovMap.put("tblNm", TABLE_POPUP);
        egovMap.put("tblOid", popupOid);

        // 첨부 파일 조회
        List<FileDTO> files = fileMapper.selectAttachmentFileByTableNameAndTablePk(egovMap);
        if (files != null && !files.isEmpty()) {
            admin614.setFiles(files.get(0));
        }

        // CKEditor 이미지 파일 조회
        List<FileDTO> ckEditorFiles = fileMapper.selectInlineFileByTableNameAndTablePk(egovMap);
        admin614.setPopupCkEditorAttachList(ckEditorFiles);

        return admin614;
    }

    @Transactional
    @Override
    public int insertAdmin614(Admin614DTO dto, MultipartFile popupFile) throws IOException {
        if (dto == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "요청 데이터가 올바르지 않습니다.");
        }

        // HTML 타입인 경우 content_text 생성
        if ("H".equals(dto.getPopupType()) && StringUtils.hasText(dto.getPopupCn())) {
            String text = Jsoup.parse(dto.getPopupCn()).text();
            text = text.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
            dto.setHtmlContentText(text);
        }

        EgovMap egovMap = new EgovMap();
        egovMap.put("content", dto.getPopupCn());
        egovMap.put("path", "popup");
        if (popupFile != null && !popupFile.isEmpty()) {
            egovMap.put("uploadFiles", List.of(popupFile));
        }
        egovMap.put("attachedFiles", dto.getAttachedFiles());
        egovMap.put("editorFiles", dto.getEditorFiles());

        String userId = SecurityUtil.getUser().getUserId();
        egovMap.put("regId", userId);
        egovMap.put("mdfcnId", userId);
        dto.setRegId(userId);
        dto.setMdfcnId(userId);

        // 1) 파일/본문 정규화 (임시 → 실제 경로 이동, CKEditor 본문 치환)
        fileService.processFiles(egovMap);

        // 처리된 htmlContent를 DTO에 다시 세팅
        if ("H".equals(dto.getPopupType())) {
            dto.setPopupCn(contentProcessService.processHtmlContent((String) egovMap.get("content")));
        }

        // 2) 팝업 저장 (selectKey로 popupOid 세팅됨)
        int result = admin614Mapper.insertAdmin614(dto);
        if (result == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "팝업 저장 에러");
        }

        // 3) file 메타 기록
        Long popupOid = dto.getPopupOid();
        egovMap.put("tblNm", TABLE_POPUP);
        egovMap.put("tblOid", popupOid);
        fileService.saveFileMeta(egovMap);

        // 팝업 캐시 무효화
        popupService.evictPopupCache();

        return result;
    }

    @Transactional
    @Override
    public int updateAdmin614(Admin614DTO dto, MultipartFile popupFile) throws IOException {
        if (dto == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "요청 데이터가 올바르지 않습니다.");
        }

        // HTML 타입인 경우 content_text 생성
        if ("H".equals(dto.getPopupType()) && StringUtils.hasText(dto.getPopupCn())) {
            dto.setHtmlContentText(HtmlUtil.stripHtml(dto.getPopupCn()));
        }

        EgovMap egovMap = new EgovMap();
        egovMap.put("content", dto.getPopupCn());
        egovMap.put("path", "popup");
        if (popupFile != null) {
            egovMap.put("uploadFiles", List.of(popupFile));
        }
        egovMap.put("attachedFiles", dto.getAttachedFiles());
        egovMap.put("editorFiles", dto.getEditorFiles());
        egovMap.put("tblNm", TABLE_POPUP);
        egovMap.put("tblOid", dto.getPopupOid());

        String userId = SecurityUtil.getUser().getUserId();
        egovMap.put("mdfcnId", userId);
        dto.setMdfcnId(userId);

        // 파일/본문 정규화
        fileService.processFiles(egovMap);

        // 처리된 htmlContent를 DTO에 다시 세팅
        if ("H".equals(dto.getPopupType())) {
            dto.setPopupCn(contentProcessService.processHtmlContent((String) egovMap.get("content")));
        }

        // file 메타 반영
        fileService.saveFileMeta(egovMap);

        int result = admin614Mapper.updateAdmin614(dto);
        if (result == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "팝업 수정 에러");
        }

        // 삭제할 파일 처리
        if (!CollectionUtils.isEmpty(dto.getDeleteAttachNos())) {
            fileService.deleteFilesByFileNos(dto.getDeleteAttachNos(), userId);
        }
        if (!CollectionUtils.isEmpty(dto.getDeleteEditorAttachNos())) {
            fileService.deleteFilesByFileNos(dto.getDeleteEditorAttachNos(), userId);
        }

        // 팝업 캐시 무효화
        popupService.evictPopupCache();

        return result;
    }

    // 614 팝업 삭제
    @Transactional
    @Override
    public void deleteAdmin614(Admin614DeleteDTO admin614DeleteDTO) {
        log.debug("delete popups: {}", admin614DeleteDTO.getPopupOids());
        try {
            int result = admin614Mapper.deleteAdmin614(admin614DeleteDTO);
            if(result == 0) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "안내(팝업) 삭제에 실패했습니다.");
            }
            String mdfcnId = admin614DeleteDTO.getMdfcnId();
            for (String popupOidStr : admin614DeleteDTO.getPopupOids()) {
                Long popupOid = Long.parseLong(popupOidStr);
                fileService.deleteFilesByTable("popup", popupOid, mdfcnId);
            }

            // 팝업 캐시 무효화
            popupService.evictPopupCache();
        } catch(BusinessException e) {
            throw e;
        } catch(Exception e) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "안내(팝업) 삭제 중 오류 발생");
        }
    }

    // 614 팝업 엑셀 다운로드
    @Override
    public ExcelExportResult admin614ExportExcel(EgovMap cond) throws IOException {
        List<Admin614ExcelDTO> admin614List = admin614Mapper.selectAdmin614ExcelList(cond);
        if(admin614List == null || admin614List.isEmpty()) {
            throw new BusinessException(ErrorCode.EXCEL_DOWNLOAD_NO_DATA, "엑셀 데이터가 존재하지 않습니다.");
        }

        String pageId = (String) cond.get("pageId");
        byte[] bytes = excelComponent.excelExportByPage(pageId, admin614List);

        ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
        String title = pageInfo.getTitle();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String strgFileNm = title + "_" + date + ".xlsx";
        return new ExcelExportResult(strgFileNm, bytes);
    }

    // 614 팝업 첨부파일 목록 조회
    @Override
    public List<FileDTO> selectAdmin614AttachListByPopupOid(Long popupOid) {
        if (popupOid == null) {
            return List.of();
        }

        // FileMapper 공통 메서드로 file 테이블에서 조회 (FileDTO 직접 반환, 변환 불필요)
        EgovMap egovMap = new EgovMap();
        egovMap.put("tblNm", "popup");
        egovMap.put("tblOid", popupOid);
        return fileMapper.selectAttachmentFileByTableNameAndTablePk(egovMap);
    }
}
