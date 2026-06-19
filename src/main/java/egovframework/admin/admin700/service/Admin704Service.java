package egovframework.admin.admin700.service;

import java.io.IOException;
import java.util.List;

import egovframework.admin.admin700.domain.QnaRegisteredStatDTO;
import egovframework.admin.admin700.domain.QnaRegisteredStatFilterDTO;
import egovframework.common.excel.domain.ExcelExportResult;

public interface Admin704Service {
	public List<QnaRegisteredStatDTO> selectQnaRegisteredStat(QnaRegisteredStatFilterDTO qnaRegisteredStatFilterDTO);
	
	public ExcelExportResult admin704ExportExcel(QnaRegisteredStatFilterDTO qnaRegisteredStatFilterDTO) throws IOException;
}
