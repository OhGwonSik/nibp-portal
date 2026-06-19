package egovframework.admin.admin700.service;

import java.io.IOException;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin700.domain.Admin701ExcelDTO;
import egovframework.admin.admin700.domain.Admin701FilterDTO;
import egovframework.admin.admin700.domain.QnaDataDTO;
import egovframework.common.excel.domain.ExcelExportResult;

public interface Admin701Service {
	public PageInfo<QnaDataDTO> selectQnaDataList(Admin701FilterDTO filter);
	public ExcelExportResult admin701ExportExcel(Admin701ExcelDTO filter) throws IOException;
}
