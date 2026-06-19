package egovframework.portal.survey.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.file.domain.FileDTO;
import egovframework.common.file.mapper.FileMapper;
import egovframework.common.file.service.FileService;
import egovframework.common.util.CryptoUtil;
import egovframework.common.util.RequestUtil;
import egovframework.portal.survey.domain.*;
import egovframework.portal.survey.mapper.SurveyMapper;
import egovframework.portal.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl extends EgovAbstractServiceImpl implements SurveyService{

	private final SurveyMapper surveyMapper;
	private final FileMapper fileMapper;
	private final FileService fileService;
	private final CryptoUtil cryptoUtil;
	
	@Override
	public PageInfo<SurveyInfoDTO> selectSurveyListWithFilter(SurveyInfoFilterDTO surveyInfoFilterDTO) {
        if (surveyInfoFilterDTO != null && surveyInfoFilterDTO.getPage() != null && surveyInfoFilterDTO.getSize() != null) {
            PageHelper.startPage(surveyInfoFilterDTO.getPage(), surveyInfoFilterDTO.getSize(), surveyInfoFilterDTO.getSortBy());
        }
        List<SurveyInfoDTO> surveyList = surveyMapper.selectSurveyListWithFilter(surveyInfoFilterDTO);
        
		return new PageInfo<>(surveyList);
	}

	@Override
	public String selectSurveyParticipatedYn(SurveyParticipateDTO surveyParticipateDTO) {
		String clientIp = RequestUtil.getRemoteIpAddress();
		
		if (clientIp == null || clientIp.isEmpty()) {
	        // IP를 식별할 수 없는 경우, 보안 정책에 따라 차단
	        throw new BusinessException(ErrorCode.ACCESS_DENIED, "접속 정보를 확인할 수 없습니다.");
	    }
		
		surveyParticipateDTO.setIpAddr(clientIp);
		
		cryptoUtil.encrypt(surveyParticipateDTO);
		Map<String, Object> surveyInfo = surveyMapper.selectSurveyDateInfo(surveyParticipateDTO);
	    if (surveyInfo == null) {
	        throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "해당 설문 정보를 찾을 수 없습니다.");
	    }
	    
	    
	    String dateStat = (String) surveyInfo.get("date_stat");
	    String surveyStat = (String) surveyInfo.get("survey_stat");

	    // 오늘 날짜 기준 기간 및 상태 체크
	    // 시작 전이거나, 종료되었거나, 상태값이 END/DEL인 경우 모두 허용되지 않은 작업으로 분류
	    if ("BEFORE".equals(dateStat)) {
	        throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "아직 설문 참여 기간이 아닙니다.");
	    }
	    
	    if ("CLOSED".equals(dateStat) || "END".equals(surveyStat) || "DEL".equals(surveyStat)) {
	        throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "이미 마감되었거나 종료된 설문입니다.");
	    }
	    
	    if("REG".equals(surveyStat)) {
	    	throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "현재 준비 중인 설문입니다.");
	    }
	    
	    int participateCount = surveyMapper.selectSurveyParticipatedYn(surveyParticipateDTO);
    
	    return (participateCount > 0) ? "Y" : "N";
	}

	@Override
	public PortalSurveyDTO selectSurveyDetail(SurveyInfoFilterDTO SurveyInfoFilterDTO) {
		//설문 조회
		PortalSurveyDTO userSurveyDetail = surveyMapper.selectSurveyDetail(SurveyInfoFilterDTO);
	    if (userSurveyDetail == null) {
	        throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "설문을 찾을 수 없습니다.");
	    }
		
	    //문항 조회
	    List<SurveyQuestionDTO> qstList = surveyMapper.selectSurveyQstList(SurveyInfoFilterDTO);
	    if (qstList == null){
	    	qstList = new ArrayList<>();
	    } 
	    
	    List<Long> srvyQitemOidList = qstList.stream()
	            .map(SurveyQuestionDTO::getSrvyQitemOid)
	            .collect(Collectors.toList());
	    
	    List<SurveyOptionDTO> optList = new ArrayList<>();
		
	    if (!srvyQitemOidList.isEmpty()) {
	    	// 옵션 조회
	        optList = surveyMapper.selectOptList(srvyQitemOidList);

	        // srvyQitemOid별 그룹핑
	        Map<Long, List<SurveyOptionDTO>> optMap =
	                optList.stream().collect(Collectors.groupingBy(SurveyOptionDTO::getSrvyQitemOid));

	        // 문항에 옵션 매핑
	        for (SurveyQuestionDTO q : qstList) {
        		List<SurveyOptionDTO> options = optMap.getOrDefault(q.getSrvyQitemOid(), new ArrayList<>());
	        	
	        	// 옵션(항목)별 첨부파일 조회 로직 (IMG_SEL 대응)
	        	for (SurveyOptionDTO opt : options) {
					EgovMap optAttachParam = new EgovMap();
					optAttachParam.put("tblNm", "srvy_qitem_opt"); // 옵션 테이블
					optAttachParam.put("tblOid", opt.getSrvyQitemOptOid());    // 옵션 번호

					List<FileDTO> optAttachList = fileMapper.selectAttachmentFileByTableNameAndTablePk(optAttachParam);
					opt.setOptionAttach(optAttachList); // DTO에 파일 리스트 세팅
				}
	        	
	            q.setOptList(options);

				// 각 문항 자체의 첨부 파일 정보 조회
				EgovMap qstAttachParam = new EgovMap();
				qstAttachParam.put("tblNm", "srvy_qitem");
				qstAttachParam.put("tblOid", q.getSrvyQitemOid());

				List<FileDTO> qstAttachList = fileMapper.selectAttachmentFileByTableNameAndTablePk(qstAttachParam);

				q.setSurveyQstAttach(qstAttachList);
	        }
	    }

	    userSurveyDetail.setQstList(qstList); 
	    
	    return userSurveyDetail;
	}

	@Override
	public int insertSurveyResponse(SurveySubmitReqDTO surveySubmitReqDTO, Map<String, MultipartFile> fileMap)
			throws IOException {
		int count = 0;
		
		//ip 추출 및 암호화
		surveySubmitReqDTO.setIpAddr(RequestUtil.getRemoteIpAddress());
		
		UserInfoDTO userInfo = cryptoUtil.decrypt(surveySubmitReqDTO.getUserInfo());
		surveySubmitReqDTO.setUserInfo(userInfo);
		surveySubmitReqDTO.setRegId("SYSTEM");
		surveySubmitReqDTO.setMdfcnId("SYSTEM");
		// insert 실행
		count = surveyMapper.insertSurveyRspd(surveySubmitReqDTO);
		if(count == 0) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "응답자 정보 저장 에러");
		}
		
		Long generatedRspdNo = surveySubmitReqDTO.getSrvyRspdntOid();
		
		// 답변 리스트(srvy_rspns) 반복 저장
		List<AnswerDTO> answerList = surveySubmitReqDTO.getAnswerList();
		
		if (answerList != null && !answerList.isEmpty()) {
			for (AnswerDTO ans : answerList) {
				
				// SurveyRespDTO 생성
				SurveyRespDTO respDTO = new SurveyRespDTO();
				respDTO.setSrvyRspdntOid(generatedRspdNo);
				respDTO.setSrvyQitemOid(ans.getSrvyQitemOid());
				respDTO.setSrvyQitemOptOid(ans.getSrvyQitemOptOid());
				respDTO.setRegId("SYSTEM");
				respDTO.setMdfcnId("SYSTEM");

				// [1] 파일 번호 저장 (DTO에서 getFileNo()로 바로 꺼냄)
	            if (ans.getFileOid() != null) {
	                respDTO.setSrvyRspnsFileNo(ans.getFileOid()); // SurveyRespDTO에도 setFileNo가 있어야 함
	            }
	            
				// 문항 타입(Type)에 따른 값 매핑
				String type = ans.getType();
				String val = ans.getAnsVal();

				if (val != null && !val.trim().isEmpty()) {
					switch (type) {
						case "RANK":
							respDTO.setSrvyRspnsRank(Integer.parseInt(val));
							break;
						case "LIKERT":
						case "RATIO":
							respDTO.setSrvyRspnsNo(new java.math.BigDecimal(val));
							break;
						case "IMG_RESP":
						    respDTO.setSrvyRspnsTxt(val);
						    break;
						default: 
							respDTO.setSrvyRspnsTxt(val);
							break;
					}
				}

				// 답변 Insert
				count = surveyMapper.insertSurveyResp(respDTO);
				if(count == 0) {
					throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "설문 응답 저장 에러");
				}
				
				Long generatedRespNo = respDTO.getSrvyRspnsOid(); 

	            // 이미지 응답(IMG_RESP)인 경우 파일 처리 로직 실행
	            if ("IMG_RESP".equals(type)) {
	                // 프론트에서 보낸 키 형식: "file_" + srvyQitemOid
	                String fileKey = "file_" + ans.getSrvyQitemOid();
	                MultipartFile file = fileMap.get(fileKey);

	                if (file != null && !file.isEmpty()) {
	                    EgovMap fileParam = new EgovMap();
	                    
	                    // 보통 단건 파일 처리를 위해 리스트나 객체로 넘김
	                    fileParam.put("path", "survey"); 
	                    fileParam.put("uploadFiles", Collections.singletonList(file)); 
	                    fileParam.put("regUserId", "SYSTEM");
	                    fileParam.put("mdfcnId", "SYSTEM");

	                    // 파일 물리 저장 및 정보 생성
	                    fileService.processFiles(fileParam);

	                    // 메타 정보 매핑 (테이블명, PK)
	                    fileParam.put("tblNm", "srvy_rspns");
	                    fileParam.put("tblOid", generatedRespNo); 
	                    
	                    // 파일 메타 저장 (file 등에 매핑 정보 저장)
	                    fileService.saveFileMeta(fileParam);
	                }
	            }
			}
		}
		return count;
	}

}
