package egovframework.admin.admin500.service;

import java.io.IOException;
import java.util.List;

import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin500.domain.Admin501SurveyDTO;
import egovframework.admin.admin500.domain.Admin501VO;
import egovframework.admin.admin500.domain.SurveySaveDTO;
import egovframework.common.excel.domain.ExcelExportResult;

public interface Admin501Service {
    PageInfo<Admin501VO> selectAdmin501List(EgovMap egovMap);
    Admin501SurveyDTO selectAdmin501Detail(EgovMap egovMap);
    int insertAdmin501SurveyCopy(Admin501VO admin501VO);
    ExcelExportResult admin501ExportExcel(EgovMap param) throws IOException;
    int updateAdmin501Stat(Admin501VO admin501VO);
    int insertAdmin501Survey(SurveySaveDTO surveySaveDTO, List<MultipartFile> questionFiles, List<MultipartFile> optionFiles) throws IOException;
    int upsertAdmin501(SurveySaveDTO surveySaveDTO, List<MultipartFile> questionFiles, List<MultipartFile> optionFiles) throws IOException;
}
