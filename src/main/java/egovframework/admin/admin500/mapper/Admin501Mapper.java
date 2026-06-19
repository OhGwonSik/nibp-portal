package egovframework.admin.admin500.mapper;

import egovframework.admin.admin500.domain.*;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.util.List;
import java.util.Map;

@Mapper
public interface Admin501Mapper {
	public List<Admin501VO> selectAdmin501List(EgovMap egovMap);
	
	public Admin501SurveyDTO selectAdmin501Detail(EgovMap egovMap);
	
	public List<SurveyQuestionDTO> selectAdmin501SurveyQstList(EgovMap egovMap);
	
	public List<SurveyOptionDTO> selectAdmin501OptList(@Param("srvyQitemOidList") List<Long> srvyQitemOidList);
	
	public int insertAdmin501Survey(SurveySaveDTO surveySaveDTO);
	
	public int insertAdmin501SurveyQst(SurveyQuestionDTO surveyQuestionDTO);

	public int insertAdmin501SurveyOpt(SurveyOptionDTO surveyOptionDTO);
	
	public int updateAdmin501Stat(Admin501VO admin501VO);
	
	public int insertSurveyCopy(Admin501SurveyCopyDTO admin501SurveyCopyDTO);
	
	public int insertQuestionCopy(Admin501SurveyCopyDTO admin501SurveyCopyDTO);
	
	public List<Admin501QstDTO> selectQstList(Long srvyOid);

	public int updateParentQst(@Param("srvyQitemOid") Long srvyQitemOid, @Param("upSrvyQitemOid")Long upSrvyQitemOid);

	public List<Admin501OptDTO> selectOptList(Long srvyOid);

	public int insertOptionCopy(Admin501OptDTO opt);

	public int updateAdmin501SurveyBase(SurveySaveDTO surveySaveDTO);

	public int upsertAdmin501SurveyQst(SurveyQuestionDTO surveyQuestionDTO);

	public int upsertAdmin501SurveyOpt(SurveyOptionDTO surveyOptionDTO);

	public int deleteAdmin501SurveyQstList(@Param("srvyQitemOidList") List<Long> srvyQitemOidList);

	public int deleteAdmin501SurveyOptList(@Param("optNoList") List<Long> optNoList);

	public void updateUpSrvyQitemOid(Map<String, Object> param);
}
