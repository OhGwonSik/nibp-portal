package egovframework.admin.admin700.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import egovframework.admin.admin700.domain.QnaAnsweredStatDTO;
import egovframework.admin.admin700.domain.QnaAnsweredStatFilterDTO;
import egovframework.admin.admin700.domain.QnaRegisteredStatDTO;
import egovframework.admin.admin700.domain.QnaRegisteredStatFilterDTO;
import egovframework.admin.admin700.mapper.Admin705Mapper;
import egovframework.admin.admin700.service.Admin705Service;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Admin705ServiceImpl extends EgovAbstractServiceImpl implements Admin705Service{

	private final Admin705Mapper admin705Mapper;
    private final ExcelComponent excelComponent;
    private final ExcelConfig excelConfig;

	@Override
	public List<QnaAnsweredStatDTO> selectQnaAnsweredStat(QnaAnsweredStatFilterDTO qnaAnsweredStatFilterDTO) {
		List<QnaAnsweredStatDTO> list = admin705Mapper.selectQnaAnsweredStat(qnaAnsweredStatFilterDTO);
        
        addTotalStatRow(list);
        
        return list;
	}
	
	@Override
	public ExcelExportResult admin705ExportExcel(QnaAnsweredStatFilterDTO qnaAnsweredStatFilterDTO) throws IOException {
		List<QnaAnsweredStatDTO> list = admin705Mapper.selectQnaAnsweredStat(qnaAnsweredStatFilterDTO);
		addTotalStatRow(list);
		
		String pageId = qnaAnsweredStatFilterDTO.getPageId();
		
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
    private void addTotalStatRow(List<QnaAnsweredStatDTO> list) {
    	
        if (list == null) {
            list = new ArrayList<>();
        }

        int totalAnswerCnt = 0;

        // 누적 합계 계산
        for (QnaAnsweredStatDTO item : list) {
            totalAnswerCnt += item.getAnsCnCnt();
        }

        // '전체' 행 객체 생성
        QnaAnsweredStatDTO totalRow = new QnaAnsweredStatDTO();
        totalRow.setSearchDate("전체"); 
        totalRow.setAnsCnCnt(totalAnswerCnt);

        // 리스트의 맨 마지막에 추가
        list.add(totalRow);
    }
}
