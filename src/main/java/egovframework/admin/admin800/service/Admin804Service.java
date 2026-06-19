package egovframework.admin.admin800.service;

import java.io.IOException;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin804filterDto;
import egovframework.common.audit.domain.ApiAccessLog;
import egovframework.common.excel.domain.ExcelExportResult;

public interface Admin804Service {
    PageInfo<ApiAccessLog> selectAccessLogWithFilter(Admin804filterDto filter);

    ExcelExportResult admin804ExportExcel(Admin804filterDto filter) throws IOException;
}