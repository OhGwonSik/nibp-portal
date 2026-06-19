package egovframework.admin.admin500.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin500.domain.*;
import egovframework.admin.admin500.mapper.Admin502Mapper;
import egovframework.admin.admin500.service.Admin502Service;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelColumnDTO;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.util.MaskingUtil;
import egovframework.common.util.SecurityUtil;
import egovframework.common.util.SortByValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class Admin502ServiceImpl extends EgovAbstractServiceImpl implements Admin502Service {
	private final Admin502Mapper admin502Mapper;
	private final ExcelConfig excelConfig;
	private final ExcelComponent excelComponent;
	private final MaskingUtil maskingUtil;

	private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of(
		"srvy_oid", "srvy_ttl", "srvy_bgng_dt", "srvy_end_dt", "reg_dt"
	);
	private static final String DEFAULT_SORT = "srvy_oid DESC";

	@Override
	public Admin502SurveyResultDTO selectAdmin502ResultList(Admin502DTO admin502DTO) {
		Long srvyOid = admin502DTO.getSrvyOid();

		Admin502SurveyResultDTO resultDTO = new Admin502SurveyResultDTO();

		// 수강 - 설문 기본 정보 조회
		Map<String, Object> basicInfo = admin502Mapper.selectAdmin502BasicInfo(admin502DTO);
		if (basicInfo != null) {
			// DB 컬럼(snake_case) -> DTO 필드(camelCase) 매핑
			if (basicInfo.get("srvy_ttl") != null) resultDTO.setSrvyTtl(String.valueOf(basicInfo.get("srvy_ttl")));

			// 날짜 연결
			if (basicInfo.get("srvy_bgng_dt") != null && basicInfo.get("srvy_end_dt") != null) {
				resultDTO.setSurveyPeriod(basicInfo.get("srvy_bgng_dt") + " ~ " + basicInfo.get("srvy_end_dt"));
			}
			if (basicInfo.get("participant_count") != null) {
				resultDTO.setParticipantCount(Integer.parseInt(String.valueOf(basicInfo.get("participant_count"))));
			}
		}

		// 설문 응답 상세 조회
		List<Map<String, Object>> rawData = admin502Mapper.selectAdmin502ResultList(srvyOid);

		// 4. 응답 데이터 가공 (Grouping)
		// 순서 보장을 위해 LinkedHashMap 사용
		Map<Long, Admin502QuestionDTO> qMap = new LinkedHashMap<>();

		for (Map<String, Object> row : rawData) {
			// Key로 사용할 문항번호 파싱
			Long srvyQitemOid = Long.valueOf(String.valueOf(row.get("srvy_qitem_oid")));
			String srvyQitemType = (String) row.get("srvy_qitem_type");

			// 문항(Question) 객체 생성 (질문당 최초 1회만 실행)
			Admin502QuestionDTO qstDTO = qMap.computeIfAbsent(srvyQitemOid, k -> {
				Admin502QuestionDTO q = new Admin502QuestionDTO();
				q.setSrvyQitemOid(srvyQitemOid);
				q.setSrvyQitemType(srvyQitemType);
				q.setSrvyQitemTtl((String) row.get("srvy_qitem_ttl"));
				q.setSrvyQitemSeq(Integer.parseInt(String.valueOf(row.get("srvy_qitem_seq"))));
				q.setPlrlChcYn((String) row.get("plrl_chc_yn"));

				// srvy_qitem_lmt 매핑 (Null 체크 필수)
			    if (row.get("srvy_qitem_lmt") != null) {
			        q.setSrvyQitemLmt(Integer.parseInt(String.valueOf(row.get("srvy_qitem_lmt"))));
			    }
				// 문항 번호 (1, 1-1, 2, 2-1 ...) 매핑
				q.setDisplayQNo((String) row.get("displayQNo"));

				// 리커트 설정값 매핑
				if ("LIKERT".equals(srvyQitemType)) {
					q.setLikertMin(row.get("likert_min") != null ? Integer.parseInt(String.valueOf(row.get("likert_min"))) : null);
					q.setLikertMax(row.get("likert_max") != null ? Integer.parseInt(String.valueOf(row.get("likert_max"))) : null);
					q.setLikertMinLbl((String) row.get("likert_min_lbl"));
					q.setLikertMaxLbl((String) row.get("likert_max_lbl"));
				}

				// 리스트 초기화 (NullPointer 방지)
				q.setOptions(new ArrayList<>());
				q.setResponses(new ArrayList<>());

				return q;
			});

			// 상세 데이터 매핑 (옵션/응답 분배)
			// 객관식 (MULTI, IMG_SEL) -> Admin502OptionDTO
			if (("MULTI".equals(srvyQitemType) || "IMG_SEL".equals(srvyQitemType)) && row.get("srvy_qitem_opt_oid") != null) {
				// 유령 데이터(텍스트 응답용 행) 필터링: opt_txt가 있을 때만 추가
				if (row.get("resp_count") != null && row.get("srvy_qitem_opt_txt") != null) {
					Admin502OptionDTO opt = new Admin502OptionDTO();
					opt.setSrvyQitemOptOid(Long.valueOf(String.valueOf(row.get("srvy_qitem_opt_oid"))));
					opt.setSrvyQitemOptTxt((String) row.get("srvy_qitem_opt_txt"));
					opt.setSrvyQitemOptSeq(Integer.parseInt(String.valueOf(row.get("srvy_qitem_opt_seq"))));
					opt.setRespCount(Long.parseLong(String.valueOf(row.get("resp_count"))));

					qstDTO.getOptions().add(opt);
				}
			}

			// 리커트 (LIKERT) -> Admin502OptionDTO (점수는 scaleValue에)
			if ("LIKERT".equals(srvyQitemType) && row.get("srvy_rspns_no") != null) {
				Admin502OptionDTO opt = new Admin502OptionDTO();
				opt.setScaleValue(((Number) row.get("srvy_rspns_no")).intValue()); // 점수
				opt.setRespCount(Long.parseLong(String.valueOf(row.get("resp_count"))));

				qstDTO.getOptions().add(opt);
			}

			// 순위형 (RANK) -> Admin502ResponseDTO
			if ("RANK".equals(srvyQitemType) && row.get("srvy_rspns_rank") != null) {
				Admin502ResponseDTO resp = new Admin502ResponseDTO();
				resp.setSrvyQitemOptTxt((String) row.get("srvy_qitem_opt_txt"));
				resp.setSrvyRspnsRank((Integer) row.get("srvy_rspns_rank"));
				resp.setRespCount(Long.parseLong(String.valueOf(row.get("resp_count"))));
				
				qstDTO.getResponses().add(resp);
			}

			// 텍스트 응답 (주관식 + 객관식 기타의견) -> Admin502ResponseDTO
			boolean hasText = row.get("srvy_rspns_txt") != null;
			boolean hasFile = row.get("strg_file_path") != null;

			if (hasText || hasFile) {
			    Admin502ResponseDTO resp = new Admin502ResponseDTO();
			    
			    // 공통 정보 매핑 (작성자, 날짜)
			    resp.setSrvyRspdntNm((String) row.get("srvy_rspdnt_nm"));
			    resp.setRegDt((String) row.get("reg_dt"));
			    
			    // 텍스트가 있으면 세팅
			    if (hasText) {
			        resp.setSrvyRspnsTxt((String) row.get("srvy_rspns_txt"));
			    }

			    // 파일 정보가 있으면 세팅 (이미지 응답형용)
			    if (hasFile) {
			        resp.setRespStrgFilePath((String) row.get("strg_file_path"));
			        resp.setRespStrgFileNm((String) row.get("strg_file_nm"));
			        resp.setRespOrgnlFileNm((String) row.get("orgnl_file_nm"));
			    }
			    
			    qstDTO.getResponses().add(resp);
			}

			if ("RATIO".equals(srvyQitemType) && row.get("srvy_qitem_opt_oid") != null) {
				// average_value가 있는 행만 유효한 통계 데이터로 취급
				if (row.get("average_value") != null) {
					Admin502OptionDTO opt = new Admin502OptionDTO();
					opt.setSrvyQitemOptOid(Long.valueOf(String.valueOf(row.get("srvy_qitem_opt_oid"))));
					opt.setSrvyQitemOptTxt((String) row.get("srvy_qitem_opt_txt"));
					// BigDecimal 등의 타입일 수 있으므로 String 변환 후 파싱
					opt.setAverageValue(Double.parseDouble(String.valueOf(row.get("average_value"))));

					qstDTO.getOptions().add(opt);
				}
			}
		}

		// 5. 최종 결과 세팅 (Map Values -> List 변환)
		resultDTO.setQuestions(new ArrayList<>(qMap.values()));

		if (resultDTO.getQuestions() != null) {
			for (Admin502QuestionDTO question : resultDTO.getQuestions()) {

				List<Admin502ResponseDTO> responseList = question.getResponses();

				if (responseList != null && !responseList.isEmpty()) {

					// 2) 마스킹 (개인정보 열람 권한이 'N'인 경우)
					if (SecurityUtil.getUser() != null && "N".equals(SecurityUtil.getUser().getPrvcUseYn())) {
						responseList = maskingUtil.maskList(responseList);
					}
					question.setResponses(responseList);
				}
			}
		}

		return resultDTO;
	}


	@Override
	public PageInfo<Admin502VO> selectAdmin502List(EgovMap egovMap) {
		int page = Integer.parseInt(String.valueOf(egovMap.get("page") == null ? 1 : egovMap.get("page")));
		int size = Integer.parseInt(String.valueOf(egovMap.get("size") == null ? 10 : egovMap.get("size")));
		String sortBy = SortByValidator.sanitize(
			String.valueOf(egovMap.get("sortBy") == null ? "" : egovMap.get("sortBy")),
			ALLOWED_SORT_COLUMNS, DEFAULT_SORT
		);

		PageHelper.startPage(page, size, sortBy);

		List<Admin502VO> list = admin502Mapper.selectAdmin502List(egovMap);
		return new PageInfo<>(list);
	}

	@Override
	public ExcelExportResult admin502ExcelDownload(Admin502ExcelDTO admin502ExcelDTO) throws IOException {

		List<Admin502QuestionDTO> questionList = admin502Mapper.selectSurveyQuestionList(admin502ExcelDTO);
		List<Admin502RawDataDTO> rawDataList = admin502Mapper.selectSurveyRawDataList(admin502ExcelDTO);

		List<Admin502RawDataDTO> maskedList = rawDataList;
		if(SecurityUtil.getUser() != null && "N".equals(SecurityUtil.getUser().getPrvcUseYn())) {
			maskedList = maskingUtil.maskList(rawDataList);
		}

		if (questionList == null || questionList.isEmpty()) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "설문 문항 정보가 없습니다.");
		}

		// ExcelComponent가 사용할 설정 객체 조립
		// 헤더 컬럼 정의 (ExcelColumnDTO)
		List<ExcelColumnDTO> headerList = new ArrayList<>();

		// 고정 컬럼
		headerList.add(createColumn("순번", "seq"));
		headerList.add(createColumn("참여자명", "srvyRspdntNm"));

		// 동적 컬럼 (문항별)
		Map<Long, String> qstKeyMap = new HashMap<>();

		int mainSeq = 0; // 대문항 카운터 (1, 2, 3...)
		int subSeq = 0;  // 소문항 카운터 (1, 2...)
		Long lastMainSrvyQitemOid = -1L; // 마지막 메인 질문의 번호 기억

		for (Admin502QuestionDTO qst : questionList) {
		    Long srvyQitemOid = qst.getSrvyQitemOid();
		    String parentType = qst.getParentSrvyQitemType(); // SQL에서 가져온 부모 타입

		    String title = "";

		    // 부모가 없거나(null), 부모가 챕터(parentType이 null)라면 -> "메인 문항"으로 취급
		    if (qst.getUpSrvyQitemOid() == null || parentType == null) {
		        mainSeq++;      // 1 -> 2 -> 3
		        subSeq = 0;     // 하위 번호 초기화
		        title = mainSeq + ". " + qst.getSrvyQitemTtl();

		        lastMainSrvyQitemOid = srvyQitemOid; // 내가 이제 누군가의 부모가 될 수 있음
		    }
		    // 부모가 진짜 질문(parentType이 있음)이라면 -> "하위 문항"으로 취급
		    else {
		        subSeq++;       // 1 -> 2
		        // "메인번호-소번호" 형식 (예: 1-1, 1-2)
		        title = mainSeq + "-" + subSeq + ". " + qst.getSrvyQitemTtl();
		    }

		    String fieldKey = "Q_" + srvyQitemOid;
		    headerList.add(createColumn(title, fieldKey));
		    qstKeyMap.put(srvyQitemOid, fieldKey);
		}

		// 임시 Page ID 생성 및 설정 등록
		// (ExcelComponent는 pageId로 설정을 찾으므로, 동적으로 만들어서 맵에 넣어줌.
		String dynamicPageId = "SURVEY_RAW_" + admin502ExcelDTO.getSrvyOid() + "_" + System.currentTimeMillis();
		String strgFileNm = "설문상세결과_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

		ExcelPageConfigDTO pageConfig = new ExcelPageConfigDTO();
		pageConfig.setTitle(strgFileNm); // 파일명 설정 (사실상 ExcelExportResult에서 덮어쓰지만 형식상)
		pageConfig.setHeader(headerList); // 만든 헤더 주입

		// 글로벌 설정 객체에 내 설정을 잠시 등록
		excelConfig.getPages().put(dynamicPageId, pageConfig);

		// 세로형 데이터 -> 가로형 Map 리스트 (Pivot)
		Map<String, Map<String, Object>> pivotMap = new LinkedHashMap<>();

		for (Admin502RawDataDTO row : maskedList) {
			String srvyRspdntOid = String.valueOf(row.getSrvyRspdntOid());
			String srvyRspdntNm = row.getSrvyRspdntNm();
			Long srvyQitemOid = row.getSrvyQitemOid();
			String answerVal = row.getAnswerVal();

			// 사용자 행 생성
			pivotMap.computeIfAbsent(srvyRspdntOid, k -> {
				Map<String, Object> m = new HashMap<>();
				m.put("srvyRspdntNm", srvyRspdntNm);
				return m;
			});

			// 답변 매핑
			String fieldKey = qstKeyMap.get(srvyQitemOid);
			if (fieldKey != null && answerVal != null && !answerVal.isEmpty()) {
				Map<String, Object> userRow = pivotMap.get(srvyRspdntOid);
				if (userRow.containsKey(fieldKey)) {
					userRow.put(fieldKey, userRow.get(fieldKey) + ", " + answerVal);
				} else {
					userRow.put(fieldKey, answerVal);
				}
			}
		}

		// 최종 리스트 변환
		List<Map<String, Object>> finalDataList = new ArrayList<>();
		int seq = 1;
		for (Map<String, Object> row : pivotMap.values()) {
			row.put("seq", seq++);
			finalDataList.add(row);
		}

		// 엑셀 생성 및 반환
		try {
			// ExcelComponent는 dynamicPageId를 보고 방금 넣은 설정을 찾아 엑셀을 만듦.
			byte[] excelBytes = excelComponent.excelExportByPage(dynamicPageId, finalDataList);

			return new ExcelExportResult(strgFileNm, excelBytes);

		} finally {
			// 임시 설정 삭제 (메모리 누수 방지)
			excelConfig.getPages().remove(dynamicPageId);
		}
	}

	// [Helper] 헤더 컬럼 생성 편의 메서드
	private ExcelColumnDTO createColumn(String label, String field) {
		ExcelColumnDTO col = new ExcelColumnDTO();
		col.setLabel(label);
		col.setField(field);
		return col;
	}


	@Override
	public List<Admin502RespondentDTO> selectSurveyRespondentList(Admin502RespondentDTO admin502RespondentDTO) {

		return admin502Mapper.selectSurveyRespondentList(admin502RespondentDTO);
	}


	@Override
	public int deleteSurveyRespondents(List<Admin502RespondentDTO> admin502RespondentList) {
		int count = 0;

		for(Admin502RespondentDTO respondent : admin502RespondentList) {

			if(respondent.getSrvyOid() == null || respondent.getSrvyOid() == null) {
				throw new BusinessException(ErrorCode.REQUIRED_FIELD_MISSING, "필수 항목이 누락되었습니다.");
			}

			count = admin502Mapper.deleteSurveyRespondents(respondent);
			if(count == 0) {
				throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "응답 삭제 에러");
			}
		}

		return count;
	}
}
