package egovframework.admin.admin600.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin600.domain.*;
import egovframework.admin.admin600.mapper.Admin606Mapper;
import egovframework.admin.admin600.service.Admin606Service;
import egovframework.common.content.ContentProcessService;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.file.service.FileService;
import egovframework.common.util.HtmlUtil;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @ClassName : Admin606ServiceImpl.java
 * @Description : FAQ 관리 서비스 구현체
 *
 * @author : balee
 * @since  : 2025. 11. 25
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Admin606ServiceImpl extends EgovAbstractServiceImpl implements Admin606Service {
	private final Admin606Mapper admin606Mapper;
	private final ExcelComponent excelComponent;
	private final ExcelConfig excelConfig;
	private final FileService fileService;
	private final ContentProcessService contentProcessService;

	private static final String TABLE_FAQ = "faq_dtl";

	@Override
	public PageInfo<Admin606VO> selectAdmin606List(Admin606FilterDTO filter) {
		PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());

		List<Admin606VO> admin606List = admin606Mapper.selectAdmin606List(filter);

		return new PageInfo<>(admin606List);
	}

	@Override
	public List<Admin606CategoryVO> selectAdmin606CategoryList() {
        return admin606Mapper.selectAdmin606CategoryList();
	}

	@Override
	public Admin606DetailDTO selectAdmin606(Long faqDtlOid) {
		Admin606VO faqInfo = admin606Mapper.selectAdmin606(faqDtlOid);
		if(faqInfo == null){
			throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "해당 faq가 없습니다.");
		}
		EgovMap egovMap = new EgovMap();
		egovMap.put("tblNm", TABLE_FAQ);
		egovMap.put("tblOid", faqDtlOid);

		// return 객체
		Admin606DetailDTO admin606DetailDTO = new Admin606DetailDTO();
		admin606DetailDTO.setFaqDtlOid(faqInfo.getFaqDtlOid());                         // 카테고리 ID (FK)
		admin606DetailDTO.setFaqCtgryOid(faqInfo.getFaqCtgryOid());
		admin606DetailDTO.setCtgryNm(faqInfo.getCtgryNm());
		admin606DetailDTO.setQstnCn(faqInfo.getQstnCn());
		admin606DetailDTO.setAnsCn(faqInfo.getAnsCn());
		admin606DetailDTO.setSortSeq(faqInfo.getSortSeq());
		admin606DetailDTO.setOpenYn(faqInfo.getOpenYn());
		admin606DetailDTO.setUseYn(faqInfo.getUseYn());
		admin606DetailDTO.setInqCnt(faqInfo.getInqCnt());
		admin606DetailDTO.setRegId(faqInfo.getRegId());
		admin606DetailDTO.setRegDt(faqInfo.getRegDt());
		admin606DetailDTO.setMdfcnId(faqInfo.getMdfcnId());
		admin606DetailDTO.setMdfcnDt(faqInfo.getMdfcnDt());

		// 첨부파일 업로드는 없기 때문에 ckeditor 파일만 조회
		admin606DetailDTO.setFaqCkEditorAttachList(fileService.selectInlineFileByTableNameAndTablePk(egovMap));

		return admin606DetailDTO;
	}

	@Override
	public void updateAdmin606inqCnt(Long faqDtlOid) {
		admin606Mapper.updateAdmin606inqCnt(faqDtlOid);
	}

	@Transactional
	@Override
	public Long insertAdmin606(Admin606DetailDTO admin606DetailDTO) {
		try {
			log.debug("insert faq: {}", admin606DetailDTO.getFaqDtlOid());

			admin606DetailDTO.setUseYn("Y");
			int result = admin606Mapper.insertAdmin606(admin606DetailDTO);
			if (result == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "FAQ 등록에 실패했습니다.");
			}

			// faqDtlOid 반환
			return admin606DetailDTO.getFaqDtlOid();
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "FAQ 등록 중 오류 발생");
		}
	}

	@Transactional
	@Override
	public void updateAdmin606(Admin606DetailDTO admin606DetailDTO) {
		try {
			log.debug("upsert faq: {}", admin606DetailDTO.getFaqDtlOid());

			int result = admin606Mapper.updateAdmin606(admin606DetailDTO);
			if (result == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "FAQ 수정에 실패했습니다.");
			}
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "FAQ 수정 중 오류 발생");
		}
	}

	@Override
	public void deleteAdmin606(Admin606DeleteDTO admin606DeleteDTO) {
		log.debug("delete faqs: {}", admin606DeleteDTO.getFaqDtlOids());
		try {
			int result = admin606Mapper.deleteAdmin606(admin606DeleteDTO);

			if(result == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "FAQ 삭제에 실패했습니다.");
			}
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "FAQ 삭제 중 오류 발생");
		}
	}

	@Override
	public ExcelExportResult admin606ExportExcel(EgovMap cond) throws IOException {
		List<Admin606ExcelDTO> admin606List = admin606Mapper.selectAdmin606ExcelList(cond);
		if(admin606List == null || admin606List.isEmpty()) {
			throw new BusinessException(ErrorCode.EXCEL_DOWNLOAD_NO_DATA, "엑셀 데이터가 존재하지 않습니다.");
		}
		
		String pageId = (String) cond.get("pageId");
		byte[] bytes = excelComponent.excelExportByPage(pageId, admin606List);

		ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
		String title = pageInfo.getTitle();
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String fileName = title + "_" + date + ".xlsx";
		return new ExcelExportResult(fileName, bytes);
	}

    @Transactional
    @Override
    public int insertFaqData(EgovMap egovMap) throws IOException {
        Admin606SaveDTO dto = (Admin606SaveDTO) egovMap.get("admin606SaveDTO");
        @SuppressWarnings("unchecked")
        List<MultipartFile> uploadFiles = (List<MultipartFile>) egovMap.get("uploadFiles");

        if (dto == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "요청 데이터가 올바르지 않습니다.");
        }

        // contents_text 생성
        dto.setAnsCnTxt(HtmlUtil.stripHtml(dto.getAnsCn()));

        String userId = SecurityUtil.getUser().getUserId();
        egovMap.clear();

        Admin606SaveDTO.populateDtoFields(egovMap, dto);
        egovMap.put("regId", userId);
        egovMap.put("mdfcnId", userId);

        // FileService.processFiles로 파일 처리 (CKEditor 본문 처리 + 파일 업로드)
        egovMap.put("path", "faq");
        egovMap.put("content", dto.getAnsCn()); // ansCn = ck에디터 본문
        egovMap.put("uploadFiles", uploadFiles);

        // DTO의 attachedFiles와 editorFiles를 FileService가 처리할 수 있는 형태로 변환
        if (!CollectionUtils.isEmpty(dto.getAttachedFiles())) {
            egovMap.put("attachedFiles", fileService.convertAttachedFileDtoToMap(dto.getAttachedFiles()));
        }
        if (!CollectionUtils.isEmpty(dto.getEditorFiles())) {
            egovMap.put("editorFiles", fileService.convertAttachedFileDtoToMap(dto.getEditorFiles()));
        }

        fileService.processFiles(egovMap);

        egovMap.put("ansCn", contentProcessService.processHtmlContent((String) egovMap.get("content")));

        // 기본 정보 DB 저장
        int result = admin606Mapper.insertFaqData(egovMap);
        if (result == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "faq 저장 실패");
        }

        Long faqDtlOid = (Long) egovMap.get("faqDtlOid");
        if (faqDtlOid == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "faq 번호 생성 실패");
        }

        // 파일 메타데이터 저장 (ATTACHMENT와 INLINE 분리)
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attachedFiles = (List<Map<String, Object>>) egovMap.get("attachedFiles");
        if (!CollectionUtils.isEmpty(attachedFiles)) {
            fileService.saveFilesByType(attachedFiles, faqDtlOid, userId, TABLE_FAQ);
        }

        return result;
    }

    @Transactional
    @Override
    public int updateFaqData(EgovMap egovMap) throws IOException {
        Admin606SaveDTO dto = (Admin606SaveDTO) egovMap.get("admin606SaveDTO");
        @SuppressWarnings("unchecked")
        List<MultipartFile> uploadFiles = (List<MultipartFile>) egovMap.get("uploadFiles");
        
        if (dto == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "요청 데이터가 올바르지 않습니다.");
        }

        // contents_text 생성
        dto.setAnsCnTxt(HtmlUtil.stripHtml(dto.getAnsCn()));

        String userId = SecurityUtil.getUser().getUserId();
        egovMap.clear();
        Admin606SaveDTO.populateDtoFields(egovMap, dto);
        egovMap.put("mdfcnId", userId);

        Long faqDtlOid = dto.getFaqDtlOid();
        if (faqDtlOid == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "faq 번호가 없습니다.");
        }

        // FileService.processFiles로 파일 처리
        egovMap.put("path", "faq");
        egovMap.put("content", dto.getAnsCn());
        egovMap.put("uploadFiles", uploadFiles);
        egovMap.put("regId", userId);

        if (!CollectionUtils.isEmpty(dto.getAttachedFiles())) {
            egovMap.put("attachedFiles", fileService.convertAttachedFileDtoToMap(dto.getAttachedFiles()));
        }
        if (!CollectionUtils.isEmpty(dto.getEditorFiles())) {
            egovMap.put("editorFiles", fileService.convertAttachedFileDtoToMap(dto.getEditorFiles()));
        }
        
        fileService.processFiles(egovMap);

        egovMap.put("ansCn", contentProcessService.processHtmlContent((String) egovMap.get("content")));

        // 기본 정보 DB 업데이트
        int result = admin606Mapper.updateFaqData(egovMap);
        if (result == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "FAQ 수정 실패");
        }

        // 삭제할 파일 처리
        if (!CollectionUtils.isEmpty(dto.getDeleteAttachNos())) {
            fileService.deleteFilesByFileNos(dto.getDeleteAttachNos(), userId);
        }
        if (!CollectionUtils.isEmpty(dto.getDeleteEditorAttachNos())) {
            fileService.deleteFilesByFileNos(dto.getDeleteEditorAttachNos(), userId);
        }

        // 새 파일이 있으면 기존 파일은 유지하고 새 파일만 추가 저장
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attachedFiles = (List<Map<String, Object>>) egovMap.get("attachedFiles");
        if (!CollectionUtils.isEmpty(attachedFiles)) {
            // 새 파일만 추가 저장 (기존 파일은 유지)
            fileService.saveFilesByType(attachedFiles, faqDtlOid, userId, TABLE_FAQ);
        }

        return result;
    }
}