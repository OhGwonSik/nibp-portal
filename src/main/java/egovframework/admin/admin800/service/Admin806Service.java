package egovframework.admin.admin800.service;

import java.io.IOException;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin806VO;
import egovframework.admin.admin800.domain.Admin806filterDto;
import egovframework.common.excel.domain.ExcelExportResult;

public interface Admin806Service {
    /**
     * 개인정보 처리 로그 조회
     * @param filter 검색 조건
     * @return 개인정보 처리 로그 목록
     */
    PageInfo<Admin806VO> selectPersonalInfoProcLogWithFilter(Admin806filterDto filter);

    ExcelExportResult admin806ExportExcel(Admin806filterDto filter) throws IOException;
}
