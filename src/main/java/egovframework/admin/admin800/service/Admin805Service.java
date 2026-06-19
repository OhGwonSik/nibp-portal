package egovframework.admin.admin800.service;

import java.io.IOException;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin805VO;
import egovframework.admin.admin800.domain.Admin805filterDto;
import egovframework.common.excel.domain.ExcelExportResult;

public interface Admin805Service {
    PageInfo<Admin805VO> selectPermissionChangeLogWithFilter(Admin805filterDto filter);

    ExcelExportResult admin805ExportExcel(Admin805filterDto filter) throws IOException;
}
