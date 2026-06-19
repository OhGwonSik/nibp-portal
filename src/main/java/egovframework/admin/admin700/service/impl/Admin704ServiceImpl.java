package egovframework.admin.admin700.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import egovframework.admin.admin700.domain.QnaRegisteredStatDTO;
import egovframework.admin.admin700.domain.QnaRegisteredStatFilterDTO;
import egovframework.admin.admin700.mapper.Admin704Mapper;
import egovframework.admin.admin700.service.Admin704Service;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Admin704ServiceImpl extends EgovAbstractServiceImpl implements Admin704Service{

	private final Admin704Mapper admin704Mapper;
    private final ExcelComponent excelComponent;
    private final ExcelConfig excelConfig;

	@Override
	public List<QnaRegisteredStatDTO> selectQnaRegisteredStat(QnaRegisteredStatFilterDTO qnaRegisteredStatFilterDTO) {
		
		List<QnaRegisteredStatDTO> list = admin704Mapper.selectQnaRegisteredStat(qnaRegisteredStatFilterDTO);
        
        addTotalStatRow(list);
        
        return list;
	}


	@Override
	public ExcelExportResult admin704ExportExcel(QnaRegisteredStatFilterDTO qnaRegisteredStatFilterDTO) throws IOException {
		List<QnaRegisteredStatDTO> list = admin704Mapper.selectQnaRegisteredStat(qnaRegisteredStatFilterDTO);
		addTotalStatRow(list);
		
		String pageId = qnaRegisteredStatFilterDTO.getPageId();
		
		byte[] bytes = excelComponent.excelExportByPage(pageId, list);
		
	    ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
	    
	    String title = pageInfo.getTitle();
	    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	    String fileName = title + "_" + date + ".xlsx";
	    
		return new ExcelExportResult(fileName, bytes);
	}
	
	/**
     * 합계(Total) 계산 및 행 추가 메소드
     */
    private void addTotalStatRow(List<QnaRegisteredStatDTO> list) {
    	
        if (list == null) {
            list = new ArrayList<>();
        }

        int totalInquiryCnt = 0;

        // 누적 합계 계산
        for (QnaRegisteredStatDTO item : list) {
            totalInquiryCnt += item.getInquiryCnt();
        }

        // '전체' 행 객체 생성
        QnaRegisteredStatDTO totalRow = new QnaRegisteredStatDTO();
        
        // 화면에 보여질 구분값 ("전체")
        totalRow.setSearchDate("전체"); 
        
        // 계산된 합계 세팅
        totalRow.setInquiryCnt(totalInquiryCnt); 

        // 리스트의 맨 마지막에 추가
        list.add(totalRow);
    }
}
