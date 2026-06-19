package egovframework.admin.admin700.service;

import java.io.IOException;
import java.util.List;

import egovframework.admin.admin700.domain.QnaCategoryStatDTO;
import egovframework.admin.admin700.domain.QnaCategoryStatFilterDTO;
import egovframework.common.excel.domain.ExcelExportResult;

public interface Admin707Service {
	public List<QnaCategoryStatDTO> selectQnaCategoryStat(QnaCategoryStatFilterDTO qnaCategoryStatFilterDTO);
	
	public ExcelExportResult admin707ExportExcel(QnaCategoryStatFilterDTO qnaCategoryStatFilterDTO) throws IOException;
}
