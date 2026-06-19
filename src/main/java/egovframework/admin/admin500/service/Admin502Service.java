package egovframework.admin.admin500.service;

import java.io.IOException;
import java.util.List;

import egovframework.admin.admin500.domain.Admin502ExcelDTO;
import egovframework.admin.admin500.domain.Admin502RespondentDTO;

import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin500.domain.Admin502DTO;
import egovframework.admin.admin500.domain.Admin502SurveyResultDTO;
import egovframework.admin.admin500.domain.Admin502VO;
import egovframework.common.excel.domain.ExcelExportResult;

public interface Admin502Service {
    
    /**
     * 조사결과 상세 조회
     * @param admin502DTO 설문 번호 및 조회 조건
     * @return Admin502SurveyResultDTO 교육/설문 기본 정보, 설문 응답 정보
     * @throws RuntimeException 조회 중 오류 발생 시
     */
	Admin502SurveyResultDTO selectAdmin502ResultList(Admin502DTO admin502DTO);

    PageInfo<Admin502VO> selectAdmin502List(EgovMap egovMap);
    
    ExcelExportResult admin502ExcelDownload(Admin502ExcelDTO admin502ExcelDTO) throws IOException;
    
    public List<Admin502RespondentDTO> selectSurveyRespondentList(Admin502RespondentDTO admin502RespondentDTO);
    
	public int deleteSurveyRespondents(List<Admin502RespondentDTO> admin502RespondentList);
}
