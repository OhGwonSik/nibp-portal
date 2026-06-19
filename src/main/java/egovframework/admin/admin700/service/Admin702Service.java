package egovframework.admin.admin700.service;

import java.io.IOException;
import java.util.Map;

import egovframework.admin.admin700.domain.Admin702ExcelDTO;
import egovframework.admin.admin700.domain.Admin702FilterDTO;
import egovframework.admin.admin700.domain.MonthlyStatisticsDataDTO;
import egovframework.common.excel.domain.ExcelExportResult;

public interface Admin702Service {
	Map<String, MonthlyStatisticsDataDTO> selectMonthlyQnaStatisticsData(Admin702FilterDTO filter);
	
	ExcelExportResult admin702ExportExcel(Admin702ExcelDTO params) throws IOException;
}
