package egovframework.admin.admin700.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import egovframework.admin.admin700.domain.Admin701ExcelDTO;
import egovframework.admin.admin700.domain.Admin701FilterDTO;
import egovframework.admin.admin700.domain.QnaDataDTO;
import egovframework.admin.admin700.mapper.Admin701Mapper;
import egovframework.admin.admin700.service.Admin701Service;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Admin701ServiceImpl extends EgovAbstractServiceImpl implements Admin701Service {
	private final Admin701Mapper admin701Mapper;
	private final ExcelConfig excelConfig;
	private final ExcelComponent excelComponent;
	
	@Override
	public PageInfo<QnaDataDTO> selectQnaDataList(Admin701FilterDTO filter) {
		
		int page = (filter.getPageIndex() != null && filter.getPageIndex() > 0) ? filter.getPageIndex() : 1;
        int size = (filter.getPageSize() != null && filter.getPageSize() > 0) ? filter.getPageSize() : 10;
        
        PageHelper.startPage(page, size);
        
        List<QnaDataDTO> qnaDataList = admin701Mapper.selectQnaDataList(filter);
		return new PageInfo<>(qnaDataList);
	}

	
	@Override
	public ExcelExportResult admin701ExportExcel(Admin701ExcelDTO params) throws IOException{
		
		List<QnaDataDTO> admin701List = admin701Mapper.selectQnaDataListForExcel(params);
	    if (admin701List == null || admin701List.isEmpty()) {
	        throw new BusinessException(ErrorCode.EXCEL_DOWNLOAD_NO_DATA, "엑셀 데이터가 존재하지 않습니다.");
	    }
		
	    String pageId = "admin701";
	    	    
	    byte[] bytes = excelComponent.excelExportByPage(pageId, admin701List);
	    
	    ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
	    
	    String title = pageInfo.getTitle();
	    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	    String fileName = title + "_" + date + ".xlsx";
	    
		return new ExcelExportResult(fileName, bytes);
	}
}
