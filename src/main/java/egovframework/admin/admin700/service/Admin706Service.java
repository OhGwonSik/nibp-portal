package egovframework.admin.admin700.service;

import java.io.IOException;
import java.util.List;

import egovframework.admin.admin700.domain.QnaAvgTimeStatDTO;
import egovframework.admin.admin700.domain.QnaAvgTimeStatFilterDTO;
import egovframework.common.excel.domain.ExcelExportResult;

public interface Admin706Service {
	public List<QnaAvgTimeStatDTO> selectQnaAvgTimeStat(QnaAvgTimeStatFilterDTO qnaAvgTimeStatFilterDTO);
	
	public ExcelExportResult admin706ExportExcel(QnaAvgTimeStatFilterDTO qnaAvgTimeStatFilterDTO) throws IOException;
}
