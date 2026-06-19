package egovframework.portal.survey.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;

import egovframework.portal.survey.domain.PortalSurveyDTO;
import egovframework.portal.survey.domain.SurveyInfoDTO;
import egovframework.portal.survey.domain.SurveyInfoFilterDTO;
import egovframework.portal.survey.domain.SurveyParticipateDTO;
import egovframework.portal.survey.domain.SurveySubmitReqDTO;

public interface SurveyService {

	PageInfo<SurveyInfoDTO> selectSurveyListWithFilter(SurveyInfoFilterDTO surveyInfoFilterDTO);
	
	String selectSurveyParticipatedYn(SurveyParticipateDTO surveyParticipateDTO);
	
	PortalSurveyDTO selectSurveyDetail(SurveyInfoFilterDTO SurveyInfoFilterDTO);
	
	int insertSurveyResponse(SurveySubmitReqDTO surveySubmitReqDTO, Map<String, MultipartFile> fileMap) throws IOException ;
}
