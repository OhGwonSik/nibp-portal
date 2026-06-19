package egovframework.portal.survey.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.portal.survey.domain.PortalSurveyDTO;
import egovframework.portal.survey.domain.SurveyInfoDTO;
import egovframework.portal.survey.domain.SurveyInfoFilterDTO;
import egovframework.portal.survey.domain.SurveyOptionDTO;
import egovframework.portal.survey.domain.SurveyParticipateDTO;
import egovframework.portal.survey.domain.SurveyQuestionDTO;
import egovframework.portal.survey.domain.SurveyRespDTO;
import egovframework.portal.survey.domain.SurveySubmitReqDTO;

@Mapper
public interface SurveyMapper {

    /**
     * 설문 리스트 조회
     * @param SurveyInfoFilterDTO 조회 조건
     * @return List<SurveyInfoDTO> 설문 리스트
     */
	public List<SurveyInfoDTO> selectSurveyListWithFilter(SurveyInfoFilterDTO surveyInfoFilterDTO);
    
    /**
     * 설문 유효성 검사
     * @param SurveyParticipateDTO
     * @return Map<String, Object>
     */
    public Map<String, Object> selectSurveyDateInfo(SurveyParticipateDTO surveyParticipateDTO);
    
    /**
     * 설문 참여 IP 확인
     * @param SurveyParticipateDTO
     * @return int
     */
    public int selectSurveyParticipatedYn(SurveyParticipateDTO surveyParticipateDTO);
    
    /**
     * 설문 상세 정보 조회
     * @param SurveyInfoDTO
     * @return SurveyInfoFilterDTO
     */
	public PortalSurveyDTO selectSurveyDetail(SurveyInfoFilterDTO surveyInfoFilterDTO);
	
    /**
     * 설문 질문 조회
     * @param SurveyInfoFilterDTO
     * @return List<SurveyQuestionDTO>
     */
	public List<SurveyQuestionDTO> selectSurveyQstList(SurveyInfoFilterDTO surveyInfoFilterDTO);
	
    /**
     * 설문 문항 조회
     * @param List<Long>
     * @return List<SurveyOptionDTO>
     */
	public List<SurveyOptionDTO> selectOptList(@Param("srvyQitemOidList") List<Long> srvyQitemOidList);
	
    /**
     * 설문 응답자 정보 저장
     * @param SurveySubmitReqDTO
     * @return int
     */
	public int insertSurveyRspd(SurveySubmitReqDTO surveySubmitReqDTO);
	
    /**
     * 설문 응답 정보 저장
     * @param SurveyRespDTO
     * @return int
     */
	public int insertSurveyResp(SurveyRespDTO surveyRespDTO);

}