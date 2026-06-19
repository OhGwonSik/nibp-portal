package egovframework.admin.admin600.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin600.domain.*;
import egovframework.admin.admin600.mapper.Admin618Mapper;
import egovframework.admin.admin600.service.Admin618Service;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.file.domain.FileDTO;
import egovframework.common.file.mapper.FileMapper;
import egovframework.common.file.service.FileDownloadService;
import egovframework.common.file.service.FileService;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName : Admin618ServiceImpl.java
 * @Description : 팝업존 관리 서비스 구현체
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Admin618ServiceImpl extends EgovAbstractServiceImpl implements Admin618Service{
	private final Admin618Mapper admin618Mapper;
	private final ExcelComponent excelComponent;
	private final ExcelConfig excelConfig;
	private final FileDownloadService fileDownloadService;
	private final FileService fileService;
	private final FileMapper fileMapper;

	@Override
	public PageInfo<Admin618VO> selectAdmin618List(Admin618FilterDTO filter) {
		PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());

		List<Admin618VO> admin618List = admin618Mapper.selectAdmin618List(filter);

		return new PageInfo<>(admin618List);
	}

	@Override
	public Admin618VO selectAdmin618(Long popupZoneOid) {
		// 조회수 증가
		admin618Mapper.updateAdmin618HitCnt(popupZoneOid);

		// 팝업존 상세 조회
		Admin618VO admin618 = admin618Mapper.selectAdmin618(popupZoneOid);

		// 팝업존 상세 첨부 파일 조회
		EgovMap egovMap = new EgovMap();
		egovMap.put("tblNm", "popup_zone");
		egovMap.put("tblOid", popupZoneOid);
		List<FileDTO> popupZoneFiles = fileMapper.selectAttachmentFileByTableNameAndTablePk(egovMap);

		admin618.setFiles(popupZoneFiles);

		return admin618;
	}

	@Transactional
	@Override
	public void insertAdmin618(Admin618DTO admin618DTO, MultipartFile thumbnailFile, List<MultipartFile> files) {
		// 팝업존 신규 등록 로직.
		// 1) 썸네일을 우선 업로드해 실제 저장경로를 확보하고 DTO.thumbnailPath에 반영한다.
		// 2) 본문/기타 첨부를 업로드 후 file에 저장한다. 썸네일과 첨부는 thmbYn 값으로 구분한다.
		// 3) 모든 파일 업로드 이후 팝업존 본문을 DB에 저장한다.
		try {
			log.debug("insert popup: {}", admin618DTO.getPopupZoneOid());
			admin618DTO.setUseYn("Y");

			// 현재 로그인한 사용자를 파일 메타데이터 reg/upd 사용자로 사용한다.
			String userId = SecurityUtil.getUser() != null ? SecurityUtil.getUser().getUserId() : "SYSTEM";
			// 썸네일 업로드 결과를 임시 보관할 Map. 썸네일이 없으면 null 유지.
			EgovMap thumbnailUploadMap = null;
			if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
				// fileService.processFiles는 uploadFiles 목록을 실제 저장소에 올리고 attachedFiles 메타를 반환한다.
				EgovMap uploadParam = new EgovMap();
				uploadParam.put("path", "popupzone");
				uploadParam.put("uploadFiles", Collections.singletonList(thumbnailFile));
				uploadParam.put("regUserId", userId);
				uploadParam.put("mdfcnId", userId);
				fileService.processFiles(uploadParam);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> attachments = (List<Map<String, Object>>) uploadParam.get("attachedFiles");
				if (!CollectionUtils.isEmpty(attachments)) {
					Map<String, Object> firstFile = attachments.get(0);
					// filePath: 저장된 경로(예: admin/popupzone/2025/04/01)
					String filePath = firstFile.get("strgFilePath") == null ? null : String.valueOf(firstFile.get("strgFilePath"));
					// storedFileName: UUID 기반 실제 파일명
					String storedFileName = firstFile.get("storedStrgFileNm") == null ? null : String.valueOf(firstFile.get("storedStrgFileNm"));
					if (filePath != null && StringUtils.hasText(filePath) && StringUtils.hasText(storedFileName)) {
						admin618DTO.setThmbPath(filePath.endsWith("/") ? filePath + storedFileName : filePath + "/" + storedFileName);
						thumbnailUploadMap = uploadParam;
					}
				}
			}

			int result = admin618Mapper.insertAdmin618(admin618DTO);
			if (result == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "팝업존 등록에 실패했습니다.");
			}

			Long popupZoneOid = admin618DTO.getPopupZoneOid();
			if (popupZoneOid == null) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "팝업존 번호 생성 실패");
			}

			if (thumbnailUploadMap != null) {
				// 썸네일 메타정보 저장: file.thumbnail_yn = 'Y'
				thumbnailUploadMap.put("tblNm", "popup_zone");
				thumbnailUploadMap.put("tblOid", popupZoneOid);
				thumbnailUploadMap.put("thmbYn", "Y");
				fileService.saveFileMeta(thumbnailUploadMap);
				
				// 썸네일 altText 즉시 업데이트
				if (StringUtils.hasText(admin618DTO.getThumbnailAltText())) {
					updateThumbnailAltText(popupZoneOid, admin618DTO.getThumbnailAltText(), userId);
				}
			}

			// hasNewUploads: FormData로 넘어온 신규 첨부, hasTempUploads: temp 업로드 응답(attachedFiles)
			boolean hasNewUploads = files != null && !files.isEmpty();
			boolean hasTempUploads = admin618DTO.getAttachedFiles() != null && !admin618DTO.getAttachedFiles().isEmpty();
			if (hasNewUploads || hasTempUploads) {
				EgovMap attachmentMap = new EgovMap();
				attachmentMap.put("path", "popupzone");
				if (hasNewUploads) {
					attachmentMap.put("uploadFiles", files);
				}
				if (hasTempUploads) {
					attachmentMap.put("attachedFiles", admin618DTO.getAttachedFiles());
				}
				attachmentMap.put("regUserId", userId);
				attachmentMap.put("mdfcnId", userId);
				fileService.processFiles(attachmentMap);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> processedAttachments = (List<Map<String, Object>>) attachmentMap.get("attachedFiles");
				if (!CollectionUtils.isEmpty(processedAttachments)) {
					attachmentMap.put("tblNm", "popup_zone");
					attachmentMap.put("tblOid", popupZoneOid);
					attachmentMap.put("thmbYn", "N");
					fileService.saveFileMeta(attachmentMap);
				}
			}

			// 일반 파일들의 altText 업데이트
			if (admin618DTO.getFileAltTexts() != null && !admin618DTO.getFileAltTexts().isEmpty()) {
				updateFileAltTexts(popupZoneOid, admin618DTO.getFileAltTexts(), userId);
			}

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.error("insertAdmin618 error", e);
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "팝업존 등록 중 오류 발생");
		}
	}

	@Transactional
	@Override
	public void updateAdmin618(Admin618DTO admin618DTO, MultipartFile thumbnailFile, List<MultipartFile> files) {
		// 팝업존 수정 로직.
		// 신규 썸네일이 넘어오면 기존 경로를 덮어쓰고, 첨부 삭제/추가 요구사항을 반영한다.
		try {
			log.debug("update popup: {}", admin618DTO.getPopupZoneOid());
			Long popupZoneOid = admin618DTO.getPopupZoneOid();
			String userId = SecurityUtil.getUser() != null ? SecurityUtil.getUser().getUserId() : "SYSTEM";

			// 수정 시에도 썸네일 업로드 로직은 등록과 동일하게 처리한다.
			EgovMap thumbnailUploadMap = null;
			if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
				// 신규 썸네일 업로드 → DTO.thumbnailPath 갱신
				EgovMap uploadParam = new EgovMap();
				uploadParam.put("path", "popupzone");
				uploadParam.put("uploadFiles", Collections.singletonList(thumbnailFile));
				uploadParam.put("regUserId", userId);
				uploadParam.put("mdfcnId", userId);
				fileService.processFiles(uploadParam);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> attachments = (List<Map<String, Object>>) uploadParam.get("attachedFiles");
				if (!CollectionUtils.isEmpty(attachments)) {
					Map<String, Object> firstFile = attachments.get(0);
					String filePath = firstFile.get("strgFilePath") == null ? null : String.valueOf(firstFile.get("strgFilePath"));
					String storedFileName = firstFile.get("storedStrgFileNm") == null ? null : String.valueOf(firstFile.get("storedStrgFileNm"));
					if (filePath != null && StringUtils.hasText(filePath) && StringUtils.hasText(storedFileName)) {
						admin618DTO.setThmbPath(filePath.endsWith("/") ? filePath + storedFileName : filePath + "/" + storedFileName);
						thumbnailUploadMap = uploadParam;
					}
				}
			}

			admin618DTO.setUseYn("Y");
			int result = admin618Mapper.updateAdmin618(admin618DTO);
			if(result == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "팝업존 수정에 실패했습니다.");
			}

			// 파일 삭제 (관리자가 기존 첨부를 제거했을 때 사용)
			handleFileDeletes(admin618DTO.getDeletedAttachNos());

			if (thumbnailUploadMap != null) {
				// 기존 썸네일(Y) 레코드는 새 썸네일을 저장하기 전에 use_yn='N'으로 비활성화한다.
				disableExistingThumbnail(popupZoneOid, userId);
				thumbnailUploadMap.put("tblNm", "popup_zone");
				thumbnailUploadMap.put("tblOid", popupZoneOid);
				thumbnailUploadMap.put("thmbYn", "Y");
				fileService.saveFileMeta(thumbnailUploadMap);
				
				// 새 썸네일의 altText 업데이트
				if (StringUtils.hasText(admin618DTO.getThumbnailAltText())) {
					updateThumbnailAltText(popupZoneOid, admin618DTO.getThumbnailAltText(), userId);
				}
			} else if (StringUtils.hasText(admin618DTO.getThumbnailAltText())) {
				// 기존 썸네일 유지하면서 altText만 업데이트
				if (admin618DTO.getThumbnailAttachNo() != null) {
					EgovMap updateParam = new EgovMap();
					updateParam.put("fileOid", admin618DTO.getThumbnailAttachNo());
					updateParam.put("imgSbstTxtCn", admin618DTO.getThumbnailAltText());
					updateParam.put("mdfcnId", userId);
					fileMapper.updateFileAltText(updateParam);
				} else {
					// thumbnailAttachNo가 없으면 조회해서 업데이트
					updateThumbnailAltText(popupZoneOid, admin618DTO.getThumbnailAltText(), userId);
				}
			}

			boolean hasNewUploads = files != null && !files.isEmpty();
			boolean hasTempUploads = admin618DTO.getAttachedFiles() != null && !admin618DTO.getAttachedFiles().isEmpty();
			if (hasNewUploads || hasTempUploads) {
				EgovMap attachmentMap = new EgovMap();
				attachmentMap.put("path", "popupzone");
				if (hasNewUploads) {
					attachmentMap.put("uploadFiles", files);
				}
				if (hasTempUploads) {
					attachmentMap.put("attachedFiles", admin618DTO.getAttachedFiles());
				}
				attachmentMap.put("regUserId", userId);
				attachmentMap.put("mdfcnId", userId);
				fileService.processFiles(attachmentMap);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> processedAttachments = (List<Map<String, Object>>) attachmentMap.get("attachedFiles");
				if (!CollectionUtils.isEmpty(processedAttachments)) {
					attachmentMap.put("tblNm", "popup_zone");
					attachmentMap.put("tblOid", popupZoneOid);
					attachmentMap.put("thmbYn", "N");
					fileService.saveFileMeta(attachmentMap);
				}
			}
			
			// 일반 파일들의 altText 업데이트
			if (admin618DTO.getFileAltTexts() != null && !admin618DTO.getFileAltTexts().isEmpty()) {
				updateFileAltTexts(popupZoneOid, admin618DTO.getFileAltTexts(), userId);
			}
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			log.error("updateAdmin618 error", e);
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "팝업존 수정 중 오류 발생");
		}
	}

	/**
	 * 썸네일 파일의 altText 업데이트
	 */
	private void updateThumbnailAltText(Long popupZoneOid, String thumbnailAltText, String userId) {
		if (popupZoneOid == null || !StringUtils.hasText(thumbnailAltText)) {
			return;
		}
		
		// 썸네일 파일 조회
		EgovMap queryParam = new EgovMap();
		queryParam.put("tblNm", "popup_zone");
		queryParam.put("tblOid", popupZoneOid);
		List<FileDTO> savedFiles = fileMapper.selectAttachmentFileByTableNameAndTablePk(queryParam);
		
		// thmbYn='Y'인 파일 찾기
		FileDTO thumbnailFile = savedFiles.stream()
			.filter(f -> "Y".equalsIgnoreCase(f.getThmbYn()))
			.findFirst()
			.orElse(null);
		
		if (thumbnailFile != null) {
			EgovMap updateParam = new EgovMap();
			updateParam.put("fileOid", thumbnailFile.getFileOid());
			updateParam.put("imgSbstTxtCn", thumbnailAltText);
			updateParam.put("mdfcnId", userId);
			fileMapper.updateFileAltText(updateParam);
			log.debug("썸네일 altText 업데이트: fileOid={}, altText={}", thumbnailFile.getFileOid(), thumbnailAltText);
		}
	}

	/**
	 * 일반 파일들의 altText 업데이트 (thmbYn='N'인 파일들)
	 */
	private void updateFileAltTexts(Long popupZoneOid, List<Admin618DTO.FileAltTextInfo> fileAltTexts, String userId) {
		// 저장된 파일 목록 조회
		EgovMap queryParam = new EgovMap();
		queryParam.put("tblNm", "popup_zone");
		queryParam.put("tblOid", popupZoneOid);
		List<FileDTO> savedFiles = fileMapper.selectAttachmentFileByTableNameAndTablePk(queryParam);
		
		// thmbYn='N'인 일반 파일들만 필터링
		List<FileDTO> normalFiles = savedFiles.stream()
			.filter(f -> "N".equalsIgnoreCase(f.getThmbYn()))
			.toList();
		
		log.debug("일반 파일 수: {}, altText 정보 수: {}", normalFiles.size(), fileAltTexts.size());

		// attachOrder 또는 fileNo로 매칭하여 altText 업데이트
		for (Admin618DTO.FileAltTextInfo altInfo : fileAltTexts) {
			FileDTO matchedFile = null;

			if (altInfo.getFileOid() != null) {
				// 기존 파일: fileNo로 매칭
				matchedFile = normalFiles.stream()
					.filter(f -> f.getFileOid().equals(altInfo.getFileOid()))
					.findFirst().orElse(null);
			} else {
				// 신규 파일: attachOrder로 매칭
				matchedFile = normalFiles.stream()
					.filter(f -> f.getAtchFileSeq() != null && f.getAtchFileSeq().equals(altInfo.getAtchFileSeq()))
					.findFirst().orElse(null);
			}

			if (matchedFile != null && StringUtils.hasText(altInfo.getImgSbstTxtCn())) {
				EgovMap updateParam = new EgovMap();
				updateParam.put("fileOid", matchedFile.getFileOid());
				updateParam.put("imgSbstTxtCn", altInfo.getImgSbstTxtCn());
				updateParam.put("mdfcnId", userId);
				fileMapper.updateFileAltText(updateParam);
				log.debug("일반 파일 altText 업데이트: fileOid={}, attachOrder={}, altText={}",
					matchedFile.getFileOid(), matchedFile.getAtchFileSeq(), altInfo.getImgSbstTxtCn());
			}
		}
	}

	/**
	 * 기존 썸네일 파일(thumbnail_yn = 'Y')이 존재하면 use_yn='N'으로 비활성화한다.
	 * 새 썸네일을 저장하기 전에 호출하여 file에 썸네일이 둘 이상 남지 않도록 보장한다.
	 * @param popupZoneOid	팝업존 PK
	 * @param userId	갱신자 ID (use_yn 변경자)
	 */
	private void disableExistingThumbnail(Long popupZoneOid, String userId) {
		if (popupZoneOid == null) {
			return;
		}
		EgovMap queryParam = new EgovMap();
		queryParam.put("tblNm", "popup_zone");
		queryParam.put("tblOid", popupZoneOid);
		List<FileDTO> attachments = fileMapper.selectAttachmentFileByTableNameAndTablePk(queryParam);
		if (CollectionUtils.isEmpty(attachments)) {
			return;
		}
		for (FileDTO file : attachments) {
			if (file == null || file.getFileOid() == null) {
				continue;
			}
			if (!"Y".equalsIgnoreCase(file.getThmbYn())) {
				continue; // 일반 첨부는 건드리지 않는다.
			}
			EgovMap deleteParam = new EgovMap();
			deleteParam.put("fileOid", file.getFileOid());
			deleteParam.put("mdfcnId", userId);
			fileMapper.deleteFileByFileNo(deleteParam); // use_yn='N' 처리
		}
	}

	// 파일 삭제
	private void handleFileDeletes(List<Long> deletedAttachNos) {
		if (deletedAttachNos == null || deletedAttachNos.isEmpty()) {
			return;
		}

		for (Long cnAttachNo : deletedAttachNos) {
			FileDTO fileDTO = fileMapper.selectFileByFileNo(cnAttachNo);

			if (fileDTO == null) {
				continue;
			}

			EgovMap deleteParam = new EgovMap();
			deleteParam.put("mdfcnId", SecurityUtil.getUser().getUserId());
			deleteParam.put("fileOid", cnAttachNo);
			int deleteResult = fileMapper.deleteFileByFileNo(deleteParam);

			if (deleteResult == 0) {
				log.warn("Failed to delete attach record: {}", cnAttachNo);
			}
		}
	}

	@Transactional
	@Override
	public void deleteAdmin618(Admin618DeleteDTO admin618DeleteDTO) {
		log.debug("delete popups: {}", admin618DeleteDTO.getPopupZoneOids());
		try {
			int result = admin618Mapper.deleteAdmin618(admin618DeleteDTO);
			if(result == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "팝업존 삭제에 실패했습니다.");
			}
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "팝업존 삭제 중 오류 발생");
		}
	}

	@Override
	public ExcelExportResult admin618ExportExcel(EgovMap cond) throws IOException {
		List<Admin618ExcelDTO> admin618List = admin618Mapper.selectAdmin618ExcelList(cond);
		if(admin618List == null || admin618List.isEmpty()) {
			throw new BusinessException(ErrorCode.EXCEL_DOWNLOAD_NO_DATA, "엑셀 데이터가 존재하지 않습니다.");
		}

		String pageId = (String) cond.get("pageId");
		byte[] bytes = excelComponent.excelExportByPage(pageId, admin618List);

		ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
		String title = pageInfo.getTitle();
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String fileName = title + "_" + date + ".xlsx";
		return new ExcelExportResult(fileName, bytes);
	}
}
