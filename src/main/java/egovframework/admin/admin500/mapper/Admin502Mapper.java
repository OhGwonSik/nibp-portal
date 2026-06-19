package egovframework.admin.admin500.mapper;

import java.util.List;
import java.util.Map;

import egovframework.admin.admin500.domain.*;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

@Mapper
public interface Admin502Mapper {

	/**
	 * 설문 응답 결과 조회
	 * @param srvyOid 설문 번호
	 * @return List<Map<String, Object>> 설문 응답 결과
	 */
	List<Map<String, Object>> selectAdmin502ResultList(Long srvyOid);
	
	/**
	 * 교육/설문 기본 정보 조회
	 * @param admin502DTO 설문 정보
	 * @return Map<String, Object> 교육 정보 상세
	 */
	Map<String, Object> selectAdmin502BasicInfo(Admin502DTO admin502DTO);
	
	public List<Admin502QuestionDTO> selectSurveyQuestionList(Admin502ExcelDTO admin502ExcelDTO);
	
	public List<Admin502RawDataDTO> selectSurveyRawDataList(Admin502ExcelDTO admin502ExcelDTO);

	public List<Admin502VO> selectAdmin502List(EgovMap egovMap);
	
	public List<Admin502RespondentDTO> selectSurveyRespondentList(Admin502RespondentDTO admin502RespondentDTO);
	
	public int deleteSurveyRespondents(Admin502RespondentDTO admin502RespondentDTO);
}
