package egovframework.admin.admin600.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin600.domain.*;
import egovframework.admin.admin600.mapper.Admin610Mapper;
import egovframework.admin.admin600.service.Admin610Service;
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
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @ClassName : Admin610ServiceImpl.java
 * @Description : Q&A 관리 서비스 구현체
 *
 * @author : balee
 * @since  : 2025. 11. 25
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Admin610ServiceImpl extends EgovAbstractServiceImpl implements Admin610Service {
	private final Admin610Mapper admin610Mapper;
	private final ExcelComponent excelComponent;
	private final ExcelConfig excelConfig;
	private final FileMapper fileMapper;
	private final FileService fileService;
	private final ContentProcessService contentProcessService;

	@Override
	public PageInfo<Admin610VO> selectAdmin610List(Admin610FilterDTO filter) {
		PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());

		List<Admin610VO> admin610List = admin610Mapper.selectAdmin610List(filter);

		return new PageInfo<>(admin610List);
	}

	@Transactional
	@Override
	public Admin610VO selectAdmin610(Long qnaOid) {
		admin610Mapper.updateAdmin610inqCnt(qnaOid);

		// qna 문의와 답변 조회
		Admin610VO admin610 = admin610Mapper.selectAdmin610(qnaOid);

		// 질문 첨부 파일 조회
		EgovMap selectQuestionParam = new EgovMap();
		selectQuestionParam.put("tblNm", "qna");
		selectQuestionParam.put("tblOid", admin610.getQnaOid());
		List<FileDTO> qnaQuestionFiles = fileMapper.selectAttachmentFileByTableNameAndTablePk(selectQuestionParam);
		admin610.setQnaQuestionFiles(qnaQuestionFiles);

		// 답변 첨부 파일 조회 (답변이 있을 경우에)
		if (admin610.getAnswers() != null && admin610.getAnswers().size() > 0) {
			EgovMap selectAnswerParam = new EgovMap();
			selectAnswerParam.put("tblNm", "qna");
			selectAnswerParam.put("tblOid", admin610.getAnswers().get(0).getQnaOid());
			List<FileDTO> qnaAnswerFiles = fileMapper.selectAttachmentFileByTableNameAndTablePk(selectAnswerParam);
			admin610.getAnswers().get(0).setFiles(qnaAnswerFiles);
		}

		return admin610;
	}

	@Transactional
	@Override
	public void upsertAdmin610(Admin610DTO admin610DTO, List<MultipartFile> files) {
		try {
			log.debug("upsert qna: {}", admin610DTO.getQnaOid());
            int count = 0;
			EgovMap egovMap = new EgovMap();
            Admin610DTO.populateDtoFields(egovMap, admin610DTO);
			egovMap.put("path", "qna");
			egovMap.put("content", admin610DTO.getQnaCn());
			egovMap.put("uploadFiles", files);

			if (!CollectionUtils.isEmpty(admin610DTO.getEditorFiles())) {
				egovMap.put("editorFiles", fileService.convertAttachedFileDtoToMap(admin610DTO.getEditorFiles()));
			}

			String userId = SecurityUtil.getUser().getUserId();
			egovMap.put("regId", userId);
			egovMap.put("mdfcnId", userId);

			// 파일 업로드 및 ckeditor 이미지 경로 치환
			fileService.processFiles(egovMap);

			// 경로 치환한 거 적용할라고 이렇게 해 놓음
			admin610DTO.setQnaCn(contentProcessService.processHtmlContent((String) egovMap.get("content")));

			int result;
			if (admin610DTO.getQnaOid() == null) {
				admin610DTO.setUseYn("Y");
				result = admin610Mapper.insertAdmin610(admin610DTO);
				
				// 답변 등록시 비밀글 여부에 따른 만족도 평가 미응답으로 insert
				if (result != 0 && admin610DTO.getUpQnaOid() != null) {
					
					// 비밀글 여부 조회
					String secretYn = admin610Mapper.selectParentSecretYn(admin610DTO);
					
					if("Y".equals(secretYn)) {
						admin610DTO.setRegId(userId);
						admin610DTO.setTblNm("qna");
						admin610DTO.setTblOid(admin610DTO.getUpQnaOid());
						count = admin610Mapper.insertQnaSatisfaction(admin610DTO);
						if(count == 0) {
							throw new BusinessException(ErrorCode.DATABASE_ERROR, "Q&A 만족도 평가 생성에 실패했습니다.");
						}
					}
					
				}
			} else {
				result = admin610Mapper.updateAdmin610(admin610DTO);
			}

			if (result == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "Q&A 저장에 실패했습니다.");
			} else {
				admin610Mapper.updateAdmin610AnswerYn(admin610DTO.getUpQnaOid());
			}

			Long qnaOid = admin610DTO.getQnaOid();
			if (qnaOid == null) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "Q&A 번호 생성 실패");
			}

			List<Long> deleteFileNos = admin610DTO.getDeletedFiles();
			if (!CollectionUtils.isEmpty(deleteFileNos)) {
				fileService.deleteFilesByFileNos(deleteFileNos, SecurityUtil.getUser().getUserId());
			}

			// file에 파일 정보 저장
			egovMap.put("tblNm", "qna");
			egovMap.put("tblOid", admin610DTO.getQnaOid());
			fileService.saveFileMeta(egovMap);
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "Q&A 저장 중 오류 발생");
		}
	}

	@Override
	public void deleteAdmin610(Admin610DeleteDTO admin610DeleteDTO) {
		log.debug("delete qna: {}", admin610DeleteDTO.getQnaOid());
		try {
			int result = admin610Mapper.deleteAdmin610(admin610DeleteDTO);

			if(result == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "Q&A 삭제에 실패했습니다.");
			}
			
			admin610Mapper.deleteAdmin610QnaSatisfaction(admin610DeleteDTO);
			
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "Q&A 삭제 중 오류 발생");
		}
	}

	@Override
	public ExcelExportResult admin610ExportExcel(EgovMap cond) throws IOException {
		List<Admin610ExcelDTO> admin610List = admin610Mapper.selectAdmin610ExcelList(cond);
		if(admin610List == null || admin610List.isEmpty()) {
			throw new BusinessException(ErrorCode.EXCEL_DOWNLOAD_NO_DATA, "엑셀 데이터가 존재하지 않습니다.");
		}

		String pageId = (String) cond.get("pageId");
		byte[] bytes = excelComponent.excelExportByPage(pageId, admin610List);

		ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
		String qnaTtl = pageInfo.getTitle();
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String strgFileNm = qnaTtl + "_" + date + ".xlsx";
		return new ExcelExportResult(strgFileNm, bytes);
	}
}