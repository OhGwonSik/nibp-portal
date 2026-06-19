package egovframework.admin.admin700.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import egovframework.admin.admin700.domain.QnaAvgTimeStatDTO;
import egovframework.admin.admin700.domain.QnaAvgTimeStatFilterDTO;
import egovframework.admin.admin700.mapper.Admin706Mapper;
import egovframework.admin.admin700.service.Admin706Service;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Admin706ServiceImpl extends EgovAbstractServiceImpl implements Admin706Service{

	private final Admin706Mapper admin706Mapper;
    private final ExcelComponent excelComponent;
    private final ExcelConfig excelConfig;
    
	@Override
	public List<QnaAvgTimeStatDTO> selectQnaAvgTimeStat(QnaAvgTimeStatFilterDTO qnaAvgTimeStatFilterDTO) {
		List<QnaAvgTimeStatDTO> list = admin706Mapper.selectQnaAvgTimeStat(qnaAvgTimeStatFilterDTO);
		
		addTotalStatRow(list);
		
		return list;
	}

	@Override
	public ExcelExportResult admin706ExportExcel(QnaAvgTimeStatFilterDTO qnaAvgTimeStatFilterDTO) throws IOException {
		List<QnaAvgTimeStatDTO> list = admin706Mapper.selectQnaAvgTimeStat(qnaAvgTimeStatFilterDTO);
		addTotalStatRow(list);
		
		String pageId = qnaAvgTimeStatFilterDTO.getPageId();
		
		byte[] bytes = excelComponent.excelExportByPage(pageId, list);
		
	    ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
	    
	    String title = pageInfo.getTitle();
	    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	    String fileName = title + "_" + date + ".xlsx";
	    
		return new ExcelExportResult(fileName, bytes);
	}
	
	/**
     * [평균 소요일 통계용] 전체 '평균' 계산 (합계 X)
     */
    private void addTotalStatRow(List<QnaAvgTimeStatDTO> list) {
        if (list == null || list.isEmpty()) {
        	list = new ArrayList<>();
        }

        double tempSum = 0.0; // 평균을 구하기 위한 임시 합계
        int activeCount = 0;  // 데이터가 있는 달의 개수

        for (QnaAvgTimeStatDTO item : list) {
            if (item.getAvgDays() > 0) {
                tempSum += item.getAvgDays();
                activeCount++;
            }
        }

        // 다 더한 값을 '달의 개수'로 나눔
        double totalAvg = 0.0;
        if (activeCount > 0) {
            totalAvg = tempSum / activeCount;
        }

        // 소수점 반올림
        totalAvg = Math.round(totalAvg * 100.0) / 100.0;

        // 결과 세팅
        QnaAvgTimeStatDTO totalRow = new QnaAvgTimeStatDTO();
        totalRow.setSearchDate("전체"); 
        totalRow.setAvgDays(totalAvg); // 합계가 아닌 평균값이 들어갑니다.

        list.add(totalRow);
    }
}
