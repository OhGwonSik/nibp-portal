package egovframework.admin.admin700.service;

import java.io.IOException;
import java.util.List;

import egovframework.admin.admin700.domain.QnaMonthlyResponseStatDTO;
import egovframework.admin.admin700.domain.QnaMonthlyResponseStatFilterDTO;
import egovframework.common.excel.domain.ExcelExportResult;

public interface Admin703Service {
	public List<QnaMonthlyResponseStatDTO> selectQnaMonthlyResponseStat(QnaMonthlyResponseStatFilterDTO qnaMonthlyResponseStatFilterDTO);
	
	public ExcelExportResult admin703ExportExcel(QnaMonthlyResponseStatFilterDTO qnaMonthlyResponseStatFilterDTO) throws IOException;
}
