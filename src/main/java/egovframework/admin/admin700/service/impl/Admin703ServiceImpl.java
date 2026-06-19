package egovframework.admin.admin700.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import egovframework.admin.admin700.domain.QnaMonthlyResponseStatDTO;
import egovframework.admin.admin700.domain.QnaMonthlyResponseStatFilterDTO;
import egovframework.admin.admin700.mapper.Admin703Mapper;
import egovframework.admin.admin700.service.Admin703Service;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Admin703ServiceImpl extends EgovAbstractServiceImpl implements Admin703Service{

	private final Admin703Mapper admin703Mapper;
    private final ExcelComponent excelComponent;
    private final ExcelConfig excelConfig;
	
	@Override
	public List<QnaMonthlyResponseStatDTO> selectQnaMonthlyResponseStat(QnaMonthlyResponseStatFilterDTO qnaMonthlyResponseStatFilterDTO) {
		List<QnaMonthlyResponseStatDTO> qnaMonthlyResponseStatList = admin703Mapper.selectQnaMonthlyResponseStat(qnaMonthlyResponseStatFilterDTO);
		addTotalStatRow(qnaMonthlyResponseStatList);
		return qnaMonthlyResponseStatList;
	}

	
    // 합계(Total) 계산 및 행 추가 메소드
    private void addTotalStatRow(List<QnaMonthlyResponseStatDTO> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        
        // 데이터가 없어도 전체 0건을 보여주기 위해 진행
        int totalDraftCnt = 0;
        double totalDraftSumDays = 0.0;
        
        int totalFinalCnt = 0;
        double totalFinalSumDays = 0.0;

        // 누적 계산
        for (QnaMonthlyResponseStatDTO item : list) {
            totalDraftCnt += item.getDraftCnt();
            totalDraftSumDays += item.getDraftSumDays();
            
            totalFinalCnt += item.getFinalCnt();
            totalFinalSumDays += item.getFinalSumDays();
        }

        // 평균 계산 (Division by Zero 방지)
        double totalDraftAvg = (totalDraftCnt == 0) ? 0.0 
                : Math.round((totalDraftSumDays / totalDraftCnt) * 100) / 100.0;
                
        double totalFinalAvg = (totalFinalCnt == 0) ? 0.0 
                : Math.round((totalFinalSumDays / totalFinalCnt) * 100) / 100.0;

        // 합계 객체 생성
        QnaMonthlyResponseStatDTO totalRow = new QnaMonthlyResponseStatDTO();
        totalRow.setSearchMonth("전체");

        totalRow.setDraftCnt(totalDraftCnt);
        totalRow.setDraftSumDays(Math.round(totalDraftSumDays * 100) / 100.0);
        totalRow.setDraftAvgDays(totalDraftAvg);

        totalRow.setFinalCnt(totalFinalCnt);
        totalRow.setFinalSumDays(Math.round(totalFinalSumDays * 100) / 100.0);
        totalRow.setFinalAvgDays(totalFinalAvg);

        // 4. 리스트에 추가
        list.add(totalRow);
    }


	@Override
	public ExcelExportResult admin703ExportExcel(QnaMonthlyResponseStatFilterDTO qnaMonthlyResponseStatFilterDTO) throws IOException {
		List<QnaMonthlyResponseStatDTO> qnaMonthlyResponseStatList = admin703Mapper.selectQnaMonthlyResponseStat(qnaMonthlyResponseStatFilterDTO);
		addTotalStatRow(qnaMonthlyResponseStatList);
		
		String pageId = qnaMonthlyResponseStatFilterDTO.getPageId();
		
		byte[] bytes = excelComponent.excelExportByPage(pageId, qnaMonthlyResponseStatList);
		
	    ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
	    
	    String title = pageInfo.getTitle();
	    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	    String fileName = title + "_" + date + ".xlsx";
	    
		return new ExcelExportResult(fileName, bytes);
	}
}
