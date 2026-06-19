package egovframework.admin.admin700.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import egovframework.admin.admin700.domain.QnaCategoryStatDTO;
import egovframework.admin.admin700.domain.QnaCategoryStatFilterDTO;
import egovframework.admin.admin700.mapper.Admin707Mapper;
import egovframework.admin.admin700.service.Admin707Service;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Admin707ServiceImpl extends EgovAbstractServiceImpl implements Admin707Service{

	private final Admin707Mapper admin707Mapper;
    private final ExcelComponent excelComponent;
    private final ExcelConfig excelConfig;
    
    
	@Override
	public List<QnaCategoryStatDTO> selectQnaCategoryStat(QnaCategoryStatFilterDTO qnaCategoryStatFilterDTO) {
		
		return admin707Mapper.selectQnaCategoryStat(qnaCategoryStatFilterDTO);
	}

	@Override
	public ExcelExportResult admin707ExportExcel(QnaCategoryStatFilterDTO qnaCategoryStatFilterDTO) throws IOException {
		List<QnaCategoryStatDTO> list = admin707Mapper.selectQnaCategoryStat(qnaCategoryStatFilterDTO);
		
		String pageId = qnaCategoryStatFilterDTO.getPageId();
		
		byte[] bytes = excelComponent.excelExportByPage(pageId, list);
		
	    ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
	    
	    String title = pageInfo.getTitle();
	    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	    String fileName = title + "_" + date + ".xlsx";
	    
		return new ExcelExportResult(fileName, bytes);
	}
	
}
