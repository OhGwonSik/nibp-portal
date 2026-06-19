package egovframework.admin.admin500.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin500.domain.*;
import egovframework.admin.admin500.mapper.Admin501Mapper;
import egovframework.admin.admin500.service.Admin501Service;
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
import egovframework.common.util.MaskingUtil;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class Admin501ServiceImpl extends EgovAbstractServiceImpl implements Admin501Service {
	private final Admin501Mapper admin501Mapper;
    private final ExcelComponent excelComponent;
    private final ExcelConfig excelConfig;
	private final MaskingUtil maskingUtil;
	private final FileMapper fileMapper;
	private final FileService fileService;
	private final ContentProcessService contentProcessService;

	@Override
	public PageInfo<Admin501VO> selectAdmin501List(EgovMap egovMap) {
		int page = Integer.parseInt(String.valueOf(egovMap.get("page") == null ? 1 : egovMap.get("page")));
		int size = Integer.parseInt(String.valueOf(egovMap.get("size") == null ? 10 : egovMap.get("size")));

		PageHelper.startPage(page, size);
		List<Admin501VO> list = admin501Mapper.selectAdmin501List(egovMap);

		if(SecurityUtil.getUser() != null && "N".equals(SecurityUtil.getUser().getPrvcUseYn())) {
			List<Admin501VO> maskedList = new ArrayList<>();
			maskedList = maskingUtil.maskList(list);
			return new PageInfo<>(maskedList);
		}
		return new PageInfo<>(list);
	}

	@Override
	public Admin501SurveyDTO selectAdmin501Detail(EgovMap egovMap) {

		//설문 조회
		Admin501SurveyDTO admin501Detail = admin501Mapper.selectAdmin501Detail(egovMap);
	    if (admin501Detail == null) {
	        throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "설문을 찾을 수 없습니다.");
	    }

	    //문항 조회
	    List<SurveyQuestionDTO> qstList = admin501Mapper.selectAdmin501SurveyQstList(egovMap);
	    if (qstList == null){
	    	qstList = new ArrayList<>();
	    }

	    List<Long> srvyQitemOidList = qstList.stream()
	            .map(SurveyQuestionDTO::getSrvyQitemOid)
	            .collect(Collectors.toList());

	    List<SurveyOptionDTO> optList = new ArrayList<>();

	    if (!srvyQitemOidList.isEmpty()) {
	    	// 옵션 조회
	        optList = admin501Mapper.selectAdmin501OptList(srvyQitemOidList);

	        // srvyQitemOid별 그룹핑
	        Map<Long, List<SurveyOptionDTO>> optMap =
	                optList.stream().collect(Collectors.groupingBy(SurveyOptionDTO::getSrvyQitemOid));

	        // 문항에 옵션 매핑
	        for (SurveyQuestionDTO q : qstList) {
	        	List<SurveyOptionDTO> options = optMap.getOrDefault(q.getSrvyQitemOid(), new ArrayList<>());

	        	// 각 옵션(항목)별 첨부파일 조회
	            for (SurveyOptionDTO opt : options) {
	                EgovMap optAttachParam = new EgovMap();
	                optAttachParam.put("tblNm", "srvy_qitem_opt");
	                optAttachParam.put("tblOid", opt.getSrvyQitemOptOid());

	                // srvy_qitem_opt 테이블에 매핑된 파일 목록 조회
	                List<FileDTO> optAttachList = fileMapper.selectAttachmentFileByTableNameAndTablePk(optAttachParam);

	                opt.setOptionAttach(optAttachList);
	            }

	            // 파일 정보가 담긴 옵션 리스트를 문항에 세팅
	            q.setOptList(options);

	            // 문항 자체의 첨부파일 조회
				EgovMap qstAttachParam = new EgovMap();
				qstAttachParam.put("tblNm", "srvy_qitem");
				qstAttachParam.put("tblOid", q.getSrvyQitemOid());

				List<FileDTO> qstAttachList = fileMapper.selectAttachmentFileByTableNameAndTablePk(qstAttachParam);
				q.setSurveyQstAttach(qstAttachList);
	        }
	    }

	    admin501Detail.setQstList(qstList);

		return admin501Detail;
	}

	@Transactional
	@Override
	public int insertAdmin501Survey(SurveySaveDTO surveySaveDTO, List<MultipartFile> questionFiles, List<MultipartFile> optionFiles) throws IOException {
		String userId = SecurityUtil.getUser().getUserId();
		int count = 0;
		surveySaveDTO.setRegId(userId);
		surveySaveDTO.setMdfcnId(userId);

		// ckeditor에 있는 이미지 처리
		EgovMap egovMap = new EgovMap();
		egovMap.put("path", "survey");
		egovMap.put("editorFiles", surveySaveDTO.getEditorFiles());
		egovMap.put("regId", userId);
		egovMap.put("mdfcnId", userId);
		egovMap.put("content", surveySaveDTO.getSrvyCn());
		fileService.processFiles(egovMap);

		// ckeditor 내용에 있는 내용 중 img 태그의 src를 temp 경로에서 실제 저장된 경로로 수정하려고
		surveySaveDTO.setSrvyCn((String) egovMap.get("content"));

		surveySaveDTO.setSrvyCn(contentProcessService.processHtmlContent(surveySaveDTO.getSrvyCn()));

		// 설문 INSERT
	    count = admin501Mapper.insertAdmin501Survey(surveySaveDTO);

		egovMap.put("tblNm", "srvy");
		egovMap.put("tblOid", surveySaveDTO.getSrvyOid());
		fileService.saveFileMeta(egovMap);

	    if (count == 0) {
	    	throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "설문 저장 실패");
	    }

	    Long srvyOid = surveySaveDTO.getSrvyOid();
	    if (srvyOid == null)
	        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "설문번호 생성 실패");

	    // displayQNo -> qst_no 매핑
	    Map<String, Long> displayToSrvyQitemOid = new HashMap<>();

	    // 챕터(레벨0) 순번
	    int chapterSeq = 0;

	    // 문항(레벨1) 전역 순번
	    int globalSrvyQitemSeq = 0;

	    // 부모문항별 하위문항 카운트 저장용
	    Map<Long, Integer> childSeqMap = new HashMap<>();

		    try {
			    for (SurveyQuestionDTO q : surveySaveDTO.getQstList()) {
		        q.setSrvyOid(srvyOid);
		        q.setRegId(userId);
		        q.setMdfcnId(userId);

		        // parent_qst_no 계산
		        Long upSrvyQitemOid = null;
		        String parentDisplay = q.getParentDisplayQNo();

		        if (parentDisplay != null && !"0".equals(parentDisplay)) {
		            upSrvyQitemOid = displayToSrvyQitemOid.get(parentDisplay);
		        }
		        q.setUpSrvyQitemOid(upSrvyQitemOid);

		        // qst_seq 계산
		        int nextSeq;
		        if (q.getLevel() == 0) {
		            // 챕터
		            nextSeq = ++chapterSeq;
		        } else if (parentDisplay != null && !parentDisplay.startsWith("CH")) {
		            // 하위문항 (level 2)
		            int childNextSeq = childSeqMap.getOrDefault(upSrvyQitemOid, 0) + 1;
		            childSeqMap.put(upSrvyQitemOid, childNextSeq);
		            nextSeq = childNextSeq;
		        } else {
		            // 일반 문항 (level 1)
		            nextSeq = ++globalSrvyQitemSeq;
		        }
		        q.setSrvyQitemSeq(nextSeq);

		        if (q.getLevel() == 0) {
					q.setEsntlYn("N"); // 챕터는 응답 불가능하므로 N
				}

		        // 문항 INSERT
		        int qCount = admin501Mapper.insertAdmin501SurveyQst(q);
		        if (qCount == 0) {
		        	throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "문항 저장 실패");
		        }

		        // 신규 문항 qst_no 매핑
		        displayToSrvyQitemOid.put(q.getDisplayQNo(), q.getSrvyQitemOid());

		        // 옵션 INSERT
		        if (q.getOptList() != null) {
		            for (SurveyOptionDTO opt : q.getOptList()) {
		                opt.setSrvyOid(srvyOid);
		                opt.setSrvyQitemOid(q.getSrvyQitemOid());
		                opt.setRegId(userId);
		                opt.setMdfcnId(userId);

		                int oCount = admin501Mapper.insertAdmin501SurveyOpt(opt);
		                if (oCount == 0) {
		                	throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "옵션 저장 실패");
		                }
		            }
		        }
		    }

			// 문항별 첨부파일 업로드(있을 경우)
			handleQuestionAttachments(surveySaveDTO.getQstList(), questionFiles, userId);
			// 옵션(이미지 선택형) 첨부파일 처리 추가
			handleOptionAttachments(surveySaveDTO.getQstList(), optionFiles, userId);
		    return surveySaveDTO.getSrvyOid().intValue();
	    } catch (IOException e) {
	    	throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "문항 첨부파일 저장 실패");
	    }
    }

	@Override
	public ExcelExportResult admin501ExportExcel(EgovMap param) throws IOException {
		List<Admin501VO> admin501List = admin501Mapper.selectAdmin501List(param);
	    if (admin501List == null || admin501List.isEmpty()) {
	        throw new BusinessException(ErrorCode.EXCEL_DOWNLOAD_NO_DATA, "엑셀 데이터가 존재하지 않습니다.");
	    }

		String pageId = (String) param.get("pageId");
		byte[] bytes = null;
		if(SecurityUtil.getUser() != null && "N".equals(SecurityUtil.getUser().getPrvcUseYn())) {
			List<Admin501VO> maskedList = new ArrayList<>();
			maskedList = maskingUtil.maskList(admin501List);
			bytes = excelComponent.excelExportByPage(pageId, maskedList);
		}else{
			bytes = excelComponent.excelExportByPage(pageId, admin501List);
		}

	    ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);

	    String title = pageInfo.getTitle();
	    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	    String strgFileNm = title + "_" + date + ".xlsx";

		return new ExcelExportResult(strgFileNm, bytes);
	}

	@Override
	public int updateAdmin501Stat(Admin501VO admin501VO) {
		int count = 0;
		String userId = SecurityUtil.getUser().getUserId();
		admin501VO.setMdfcnId(userId);
		admin501VO.setSrvyStts("DEL");

		count = admin501Mapper.updateAdmin501Stat(admin501VO);
		if(count == 0) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "설문 삭제 실패");
		}

		return 0;
	}

	@Override
	public int insertAdmin501SurveyCopy(Admin501VO admin501VO) {
		int count = 0;
		String userId = SecurityUtil.getUser().getUserId();

	    Admin501SurveyCopyDTO admin501SurveyCopyDTO = new Admin501SurveyCopyDTO();
	    admin501SurveyCopyDTO.setOldSrvyOid(admin501VO.getSrvyOid());
	    admin501SurveyCopyDTO.setNewSrvyTtl(admin501VO.getSrvyTtl());
	    admin501SurveyCopyDTO.setUserId(userId);

	    if(admin501VO.getSrvyBgngDt() != null && admin501VO.getSrvyEndDt() != null) {
	    	admin501SurveyCopyDTO.setSrvyBgngDt(admin501VO.getSrvyBgngDt());
	    	admin501SurveyCopyDTO.setSrvyEndDt(admin501VO.getSrvyEndDt());
	    }

	     // 설문 복사 (srvy)
	    int surveyCount = admin501Mapper.insertSurveyCopy(admin501SurveyCopyDTO);
	    if (surveyCount == 0 || admin501SurveyCopyDTO.getNewSrvyOid() == null) {
	        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "설문 복사 실패");
	    }

	    Long oldSrvyOid = admin501SurveyCopyDTO.getOldSrvyOid();
	    Long newSrvyOid = admin501SurveyCopyDTO.getNewSrvyOid();

	     // 문항 복사 (srvy_qitem)
	    int qstCount = admin501Mapper.insertQuestionCopy(admin501SurveyCopyDTO);
	    if (qstCount == 0) {
	        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "문항 복사 실패");
	    }

	     // old/new 문항 조회
	    List<Admin501QstDTO> oldList = admin501Mapper.selectQstList(oldSrvyOid);
	    List<Admin501QstDTO> newList = admin501Mapper.selectQstList(newSrvyOid);

	    if (oldList.size() != newList.size()) {
	        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "문항 매핑 개수 오류");
	    }

	    // parent_qst_no 매핑 업데이트
	    for (int i = 0; i < oldList.size(); i++) {

	        Admin501QstDTO oldQ = oldList.get(i);
	        Admin501QstDTO newQ = newList.get(i);

	        Long oldParent = oldQ.getUpSrvyQitemOid();
	        if (oldParent == null) continue;

	        Long newParent = null;

	        for (int j = 0; j < oldList.size(); j++) {
	            if (oldList.get(j).getSrvyQitemOid().equals(oldParent)) {
	                newParent = newList.get(j).getSrvyQitemOid();
	                break;
	            }
	        }

	        if (newParent == null) {
	            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "부모 문항 매핑 실패");
	        }

	        count = admin501Mapper.updateParentQst(newQ.getSrvyQitemOid(), newParent);
	        if (count == 0) {
	            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "부모 문항 업데이트 실패");
	        }
	    }

	    // 선택지 복사 (srvy_qitem_opt)
	    List<Admin501OptDTO> oldOptList = admin501Mapper.selectOptList(oldSrvyOid);

	    for (Admin501OptDTO opt : oldOptList) {

	        Long oldSrvyQitemOid = opt.getSrvyQitemOid();
	        Long newSrvyQitemOid = null;

	        for (int i = 0; i < oldList.size(); i++) {
	            if (oldList.get(i).getSrvyQitemOid().equals(oldSrvyQitemOid)) {
	                newSrvyQitemOid = newList.get(i).getSrvyQitemOid();
	                break;
	            }
	        }

	        if (newSrvyQitemOid == null) {
	            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "선택지 문항 매핑 실패");
	        }

	        opt.setSrvyQitemOid(newSrvyQitemOid);
	        opt.setRegId(userId);

	        count = admin501Mapper.insertOptionCopy(opt);
	        if (count == 0) {
	            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "선택지 저장 실패");
	        }
	    }

		return newSrvyOid.intValue();
	}

	@Transactional
	@Override
	public int upsertAdmin501(SurveySaveDTO surveySaveDTO, List<MultipartFile> questionFiles, List<MultipartFile> optionFiles) throws IOException {
		int count = 0;
		String userId = SecurityUtil.getUser().getUserId();
		surveySaveDTO.setRegId(userId);
	    surveySaveDTO.setMdfcnId(userId);

	    // 기본 검증
	    if (surveySaveDTO.getSrvyOid() == null) {
	        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "설문번호(SrvyOid)가 누락되어 수정 처리가 불가능합니다.");
	    }

		// ckeditor에 있는 이미지 처리
		EgovMap egovMap = new EgovMap();
		egovMap.put("path", "survey");
		egovMap.put("editorFiles", surveySaveDTO.getEditorFiles());
		egovMap.put("regId", userId);
		egovMap.put("mdfcnId", userId);
		egovMap.put("content", surveySaveDTO.getSrvyCn());
		fileService.processFiles(egovMap);

		egovMap.put("tblNm", "srvy");
		egovMap.put("tblOid", surveySaveDTO.getSrvyOid());
		fileService.saveFileMeta(egovMap);

		surveySaveDTO.setSrvyCn((String) egovMap.get("content"));

		surveySaveDTO.setSrvyCn(contentProcessService.processHtmlContent(surveySaveDTO.getSrvyCn()));

	    // 설문 기본 정보 UPDATE
	    count = admin501Mapper.updateAdmin501SurveyBase(surveySaveDTO);
	    if (count == 0) {
	        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "설문 수정 실패");
	    }

	    // 삭제 리스트 처리
	    if (surveySaveDTO.getDeletedOptList() != null && !surveySaveDTO.getDeletedOptList().isEmpty()) {
	        admin501Mapper.deleteAdmin501SurveyOptList(surveySaveDTO.getDeletedOptList());
	    }

	    if (surveySaveDTO.getDeletedQstList() != null && !surveySaveDTO.getDeletedQstList().isEmpty()) {
	        admin501Mapper.deleteAdmin501SurveyQstList(surveySaveDTO.getDeletedQstList());
	    }

	    // 문항 첨부 삭제 처리
	    // - 프론트에서 기존 첨부(X)를 클릭하면 해당 file_no 목록을 deletedQstAttachList로 보냄.
	    // - fileService.deleteFilesByFileNos는 물리 파일 삭제 후 file.use_yn='N'으로 논리 삭제한다.
	    // - qst/opt 삭제와 별개로 첨부만 제거하는 시나리오를 지원한다.
	    if (!CollectionUtils.isEmpty(surveySaveDTO.getDeletedQstAttachList())) {
	        fileService.deleteFilesByFileNos(surveySaveDTO.getDeletedQstAttachList(), userId);
	    }

	    // displayQNo -> qst_no 매핑을 위한 Map
	    Map<String, Long> displayToSrvyQitemOid = new HashMap<>();

	    int chapterSeq = 0;
	    int globalSrvyQitemSeq = 0;
	    Map<Long, Integer> childSeqMap = new HashMap<>();

	    // 1차 순회: 모든 문항 UPSERT (displayToSrvyQitemOid Map 구축)
	    for (SurveyQuestionDTO q : surveySaveDTO.getQstList()) {
	        q.setSrvyOid(surveySaveDTO.getSrvyOid());
	        q.setRegId(userId);
	        q.setMdfcnId(userId);

	        if (q.getLevel() == 0) {
	            q.setEsntlYn("N"); // 챕터는 필수 아님
	        }

	        // qst_seq 재계산
	        int nextSeq;
	        if (q.getLevel() == 0) {
	            nextSeq = ++chapterSeq;
	        } else if (q.getUpSrvyQitemOid() != null && q.getDisplayQNo() != null && q.getDisplayQNo().contains("-")) {
	            int childNextSeq = childSeqMap.getOrDefault(q.getUpSrvyQitemOid(), 0) + 1;
	            childSeqMap.put(q.getUpSrvyQitemOid(), childNextSeq);
	            nextSeq = childNextSeq;
	        } else {
	            nextSeq = ++globalSrvyQitemSeq;
	        }
	        q.setSrvyQitemSeq(nextSeq);

	        // 문항 UPSERT (parentSrvyQitemOid는 아직 확정 안 함)
	        admin501Mapper.upsertAdmin501SurveyQst(q);
	        if (q.getSrvyQitemOid() == null) {
	            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "문항 SrvyQitemOid 생성 실패");
	        }

	        // displayQNo -> srvyQitemOid 매핑 저장
	        if (q.getDisplayQNo() != null) {
	            displayToSrvyQitemOid.put(q.getDisplayQNo(), q.getSrvyQitemOid());
	        }
	    }

	    // 2차 순회: upSrvyQitemOid 재계산 및 UPDATE
	    for (SurveyQuestionDTO q : surveySaveDTO.getQstList()) {

	        // parentDisplayQNo 기반으로 실제 upSrvyQitemOid 계산 (INSERT 로직과 동일)
	        Long calculatedUpSrvyQitemOid = null;
	        String parentDisplay = q.getParentDisplayQNo();

	        if (parentDisplay != null && !"0".equals(parentDisplay)) {
	            calculatedUpSrvyQitemOid = displayToSrvyQitemOid.get(parentDisplay);

	            if (calculatedUpSrvyQitemOid == null) {
	                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
	                    "부모 문항을 찾을 수 없습니다: " + parentDisplay);
	            }
	        }

	        // 계산된 parentSrvyQitemOid가 기존 값과 다르면 UPDATE
	        if (!Objects.equals(calculatedUpSrvyQitemOid, q.getUpSrvyQitemOid())) {
	            q.setUpSrvyQitemOid(calculatedUpSrvyQitemOid);

	            // parentSrvyQitemOid만 업데이트하는 쿼리 실행
	            Map<String, Object> updateParam = new HashMap<>();
	            updateParam.put("srvyQitemOid", q.getSrvyQitemOid());
	            updateParam.put("upSrvyQitemOid", calculatedUpSrvyQitemOid);
	            updateParam.put("mdfcnId", userId);

	            admin501Mapper.updateUpSrvyQitemOid(updateParam);
	        }

	        // 옵션 UPSERT
	        if (q.getOptList() != null) {
	            for (SurveyOptionDTO opt : q.getOptList()) {
	                opt.setSrvyOid(q.getSrvyOid());
	                opt.setSrvyQitemOid(q.getSrvyQitemOid());
	                opt.setRegId(userId);
	                opt.setMdfcnId(userId);

	                admin501Mapper.upsertAdmin501SurveyOpt(opt);
	            }
	        }
	    }

	    try {
	        if (questionFiles != null && !questionFiles.isEmpty()) {
	            for (SurveyQuestionDTO q : surveySaveDTO.getQstList()) {
	                Integer originalIndex = q.getAttachFileIndex();

	                // 인덱스 유효성 검사
	                if (originalIndex != null && originalIndex >= 0 && originalIndex < questionFiles.size()) {
	                    MultipartFile targetFile = questionFiles.get(originalIndex);

	                    // 파일이 비어있지 않은 경우에만 처리
	                    if (targetFile != null && !targetFile.isEmpty()) {

	                        // 해당 파일 하나만 담긴 리스트 생성
	                        List<MultipartFile> singleFileList = Collections.singletonList(targetFile);

	                        // handleQuestionAttachments는 내부에서 q.getAttachFileIndex()를 사용해 파일을 찾음.
	                        //    우리가 넘겨줄 리스트는 size가 1이므로, 인덱스를 잠시 0으로 변경해야 함.
	                        q.setAttachFileIndex(0);
	                        List<SurveyQuestionDTO> singleQstList = Collections.singletonList(q);

	                        // 헬퍼 메서드 호출 (파일 1개, 문항 1개만 전달)
	                        handleQuestionAttachments(singleQstList, singleFileList, userId);

	                        // 로직 종료 후 원래 인덱스로 복구
	                        q.setAttachFileIndex(originalIndex);
	                    }
	                }
	            }
	        }
		    // 옵션 첨부파일 처리 호출
			handleOptionAttachments(surveySaveDTO.getQstList(), optionFiles, userId);
	    } catch (IOException e) {
	        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "문항 첨부파일 저장 실패");
	    }

	    return surveySaveDTO.getSrvyOid().intValue();
	}

	/**
	 * qstList에 포함된 첨부파일 인덱스를 이용해 실제 파일을 업로드하고
	 * srvy_qst_attach 테이블에 매핑 정보를 저장한다.
	 */
	private void handleQuestionAttachments(List<SurveyQuestionDTO> qstList, List<MultipartFile> questionFiles, String userId) throws IOException {
		if (CollectionUtils.isEmpty(qstList) || CollectionUtils.isEmpty(questionFiles)) {
			return;
		}

		for (SurveyQuestionDTO question : qstList) {
			Integer fileIndex = question.getAttachFileIndex();
			if (fileIndex == null || fileIndex < 0) {
				continue;
			}
			if (fileIndex >= questionFiles.size() || question.getSrvyQitemOid() == null) {
				continue;
			}

			MultipartFile file = questionFiles.get(fileIndex);
			if (file == null || file.isEmpty()) {
				continue;
			}

			// 동일 문항의 기존 첨부가 있다면 먼저 비활성화 처리
			EgovMap deleteParam = new EgovMap();
			deleteParam.put("tblNm", "srvy_qitem");
			deleteParam.put("tblOid", question.getSrvyQitemOid());
			deleteParam.put("mdfcnId", userId);
			log.info("deleteParam =================> {}", deleteParam);
			fileMapper.deleteFileByTableNameAndTablePk(deleteParam);

			EgovMap insertParam = new EgovMap();
			insertParam.put("path", "survey/question");
			insertParam.put("uploadFiles", questionFiles);
			insertParam.put("tblNm", "srvy_qitem");		// 어떤 테이블의 첨부 파일인지
			insertParam.put("tblOid", question.getSrvyQitemOid());	// 어떤 테이블의 어떤 pk의 첨부 파일인지

			insertParam.put("regId", userId);
			insertParam.put("mdfcnId", userId);

			fileService.processFiles(insertParam);
			fileService.saveFileMeta(insertParam);
		}
	}

	/**
	 * 옵션(선택지)별 첨부파일 처리 (이미지 선택형 문항용)
	 * srvy_qitem_opt 테이블에 파일을 매핑합니다.
	 */
	private void handleOptionAttachments(List<SurveyQuestionDTO> qstList, List<MultipartFile> optionFiles, String userId) throws IOException {
		if (CollectionUtils.isEmpty(qstList) || CollectionUtils.isEmpty(optionFiles)) {
			return;
		}

		for (SurveyQuestionDTO q : qstList) {
			// 옵션이 없으면 패스
			if (CollectionUtils.isEmpty(q.getOptList())) {
				continue;
			}

			for (SurveyOptionDTO opt : q.getOptList()) {
				Integer fileIndex = opt.getAttachFileIndex(); // DTO에 이 필드가 있어야 함

				// 인덱스 유효성 검사
				if (fileIndex == null || fileIndex < 0 || fileIndex >= optionFiles.size()) {
					continue;
				}
				if (opt.getSrvyQitemOptOid() == null) {
					continue;
				}

				MultipartFile file = optionFiles.get(fileIndex);
				if (file == null || file.isEmpty()) {
					continue;
				}

				// 기존 파일 비활성화 (수정 시 기존 이미지 삭제 후 재등록)
				EgovMap deleteParam = new EgovMap();
				deleteParam.put("tblNm", "srvy_qitem_opt");
				deleteParam.put("tblOid", opt.getSrvyQitemOptOid());
				deleteParam.put("mdfcnId", userId);
				fileMapper.deleteFileByTableNameAndTablePk(deleteParam);

				// 2. 신규 파일 등록
				EgovMap insertParam = new EgovMap();
				insertParam.put("path", "survey/option"); // 저장 경로 구분

				// 해당 파일 하나만 리스트에 담아 전달 (중복 저장 방지)
				insertParam.put("uploadFiles", Collections.singletonList(file));

				insertParam.put("tblNm", "srvy_qitem_opt");
				insertParam.put("tblOid", opt.getSrvyQitemOptOid());
				insertParam.put("regId", userId);
				insertParam.put("mdfcnId", userId);

				fileService.processFiles(insertParam);
				fileService.saveFileMeta(insertParam);
			}
		}
	}	

}
