package egovframework.admin.admin700.service;

import java.io.IOException;
import java.util.List;

import egovframework.admin.admin700.domain.Admin708ExcelDTO;
import egovframework.admin.admin700.domain.Admin708FilterDTO;
import egovframework.admin.admin700.domain.QnaSatisfactionStatistcsDTO;
import egovframework.common.excel.domain.ExcelExportResult;

public interface Admin708Service {
    public List<QnaSatisfactionStatistcsDTO> selectQnaSatisfactionStatisticsData(Admin708FilterDTO filter);
    public ExcelExportResult admin708ExportExcel(Admin708ExcelDTO params) throws IOException;
}
