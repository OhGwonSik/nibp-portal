package egovframework.admin.admin700.service;

import java.io.IOException;
import java.util.List;

import egovframework.admin.admin700.domain.QnaAnsweredStatDTO;
import egovframework.admin.admin700.domain.QnaAnsweredStatFilterDTO;
import egovframework.common.excel.domain.ExcelExportResult;

public interface Admin705Service {
	public List<QnaAnsweredStatDTO> selectQnaAnsweredStat(QnaAnsweredStatFilterDTO qnaAnsweredStatFilterDTO);
	
	public ExcelExportResult admin705ExportExcel(QnaAnsweredStatFilterDTO qnaAnsweredStatFilterDTO) throws IOException;
}
