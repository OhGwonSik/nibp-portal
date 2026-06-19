package egovframework.admin.admin800.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin804filterDto;
import egovframework.admin.admin800.mapper.Admin804Mapper;
import egovframework.admin.admin800.service.Admin804Service;
import egovframework.common.audit.domain.ApiAccessLog;
import egovframework.common.component.AESComponent;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.util.MaskingUtil;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Admin804ServiceImpl extends EgovAbstractServiceImpl implements Admin804Service {
    private final Admin804Mapper admin804Mapper;
    private final MaskingUtil maskingUtil;
    private final AESComponent aesComponent;
    private final ExcelComponent excelComponent;
    private final ExcelConfig excelConfig;

    @Override
    public PageInfo<ApiAccessLog> selectAccessLogWithFilter(Admin804filterDto filter) {
		if(filter.getPage() != null && filter.getPage() > 0 && filter.getSize() != null && filter.getSize() > 0) {
			PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
		}

        List<ApiAccessLog> searchList = admin804Mapper.selectAccessLogWithFilter(filter, aesComponent.getSecretKey());

        if("N".equals(SecurityUtil.getUser().getPrvcUseYn())){
            List<ApiAccessLog> maskedList = new ArrayList<>();
            maskedList = maskingUtil.maskList(searchList);
            return new PageInfo<>(maskedList);
        }

        return new PageInfo<>(searchList);
    }

    @Override
    public ExcelExportResult admin804ExportExcel(Admin804filterDto filter) throws IOException {
        List<ApiAccessLog> list = admin804Mapper.selectAccessLogWithFilter(filter, aesComponent.getSecretKey());

        if (list == null || list.isEmpty()) {
            throw new BusinessException(ErrorCode.EXCEL_DOWNLOAD_NO_DATA, "엑셀 데이터가 존재하지 않습니다.");
        }

        String pageId = "admin804";
        byte[] bytes = excelComponent.excelExportByPage(pageId, list);

        ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
        String title = pageInfo.getTitle();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = title + "_" + date + ".xlsx";

        return new ExcelExportResult(fileName, bytes);
    }
}