package egovframework.admin.admin700.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import egovframework.admin.admin700.domain.Admin702ExcelDTO;
import egovframework.admin.admin700.domain.Admin702FilterDTO;
import egovframework.admin.admin700.domain.MonthlyQnaStatisticsDataDTO;
import egovframework.admin.admin700.domain.MonthlyStatisticsDataDTO;
import egovframework.admin.admin700.domain.MonthlyVisitorStatisticsDataDTO;
import egovframework.admin.admin700.mapper.Admin702Mapper;
import egovframework.admin.admin700.service.Admin702Service;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Admin702ServiceImpl extends EgovAbstractServiceImpl implements Admin702Service {
	private final Admin702Mapper admin702Mapper;
	private final ExcelConfig excelConfig;
	private final ExcelComponent excelComponent;
	
	// admin702 통계데이터 조회
	@Override
	public Map<String, MonthlyStatisticsDataDTO> selectMonthlyQnaStatisticsData(Admin702FilterDTO filter) {
        List<MonthlyQnaStatisticsDataDTO> monthlyQnaData = admin702Mapper.selectMonthlyQnaStatisticsData(filter);
		List<MonthlyVisitorStatisticsDataDTO> monthlyVisitorData = admin702Mapper.selectMonthlyVisitorStatisticsData(filter);
		
		return this.mergeStatisticsData(monthlyQnaData, monthlyVisitorData);
	}

    // 통계데이터 합치기
	private Map<String, MonthlyStatisticsDataDTO> mergeStatisticsData(List<MonthlyQnaStatisticsDataDTO> monthlyQnaData, List<MonthlyVisitorStatisticsDataDTO> monthlyVisitorData) {
        Map<String, MonthlyStatisticsDataDTO> periodDataMap = new HashMap<>();

        // Initialize map with empty DTOs for all expected periods
        periodDataMap.put("YEARLY", new MonthlyStatisticsDataDTO()); // 해당년(누적)
        periodDataMap.put("CURRENT", new MonthlyStatisticsDataDTO()); // 당월
        periodDataMap.put("PREVIOUS", new MonthlyStatisticsDataDTO()); // 전월
        periodDataMap.put("LAST_YEAR", new MonthlyStatisticsDataDTO()); // 전년 동월

        // QNA 통계데이터 합치기
        for (MonthlyQnaStatisticsDataDTO qnaData : monthlyQnaData) {
            MonthlyStatisticsDataDTO mergedData = periodDataMap.get(qnaData.getPeriodType());
            if (mergedData != null) { 
                mergedData.setPeriodType(qnaData.getPeriodType());
                mergedData.setMonthGrp(qnaData.getMonthGrp());
                mergedData.setQuestionCount(qnaData.getQuestionCount());
                mergedData.setDraftCount(qnaData.getDraftCount());
                mergedData.setFinalCount(qnaData.getFinalCount());
                mergedData.setDraftAverageDate(qnaData.getDraftAverageDate());
                mergedData.setFinalAverageDate(qnaData.getFinalAverageDate());
            }
        }

        // 방문자 통계데이터 합치기
        for (MonthlyVisitorStatisticsDataDTO visitorData : monthlyVisitorData) {
            MonthlyStatisticsDataDTO mergedData = periodDataMap.get(visitorData.getPeriodType());
            if (mergedData != null) { 
                mergedData.setPeriodType(visitorData.getPeriodType());
                mergedData.setMonthGrp(visitorData.getMonthGrp()); 
                mergedData.setVisitor(visitorData.getVisitor());
                mergedData.setDailyVisitorAverage(visitorData.getDailyVisitorAverage());
            }
        }

        // 계산을 위한 변수 할당
        MonthlyStatisticsDataDTO current = periodDataMap.get("CURRENT");
        MonthlyStatisticsDataDTO previous = periodDataMap.get("PREVIOUS");
        MonthlyStatisticsDataDTO lastYear = periodDataMap.get("LAST_YEAR");

        // MoM 증감율 계산을 위한 DTO
        MonthlyStatisticsDataDTO momChangeDto = new MonthlyStatisticsDataDTO();
        momChangeDto.setMonthGrp("전월대비 증감");
        momChangeDto.setPeriodType("MOM_CHANGE");
        
        // YoY 증감율 계산을 위한 DTO
        MonthlyStatisticsDataDTO yoyChangeDto = new MonthlyStatisticsDataDTO();
        yoyChangeDto.setMonthGrp("전년동월대비 증감");
        yoyChangeDto.setPeriodType("YOY_CHANGE");


        if (current != null) {
            // 차이계산
            momChangeDto.setQuestionCount(calculateDifference(current.getQuestionCount(), previous != null ? previous.getQuestionCount() : null)); // 질문
            momChangeDto.setDraftCount(calculateDifference(current.getDraftCount(), previous != null ? previous.getDraftCount() : null)); // 초안
            momChangeDto.setFinalCount(calculateDifference(current.getFinalCount(), previous != null ? previous.getFinalCount() : null)); // 최종
            momChangeDto.setDraftAverageDate(calculateDifference(current.getDraftAverageDate(), previous != null ? previous.getDraftAverageDate() : null)); // 초안평균일
            momChangeDto.setFinalAverageDate(calculateDifference(current.getFinalAverageDate(), previous != null ? previous.getFinalAverageDate() : null)); // 최종평균일
            momChangeDto.setVisitor(calculateDifference(current.getVisitor(), previous != null ? previous.getVisitor() : null)); // 방문자
            momChangeDto.setDailyVisitorAverage(calculateDifference(current.getDailyVisitorAverage(), previous != null ? previous.getDailyVisitorAverage() : null));  // 일일 방문자 평균

            yoyChangeDto.setQuestionCount(calculateDifference(current.getQuestionCount(), lastYear != null ? lastYear.getQuestionCount() : null)); // 질문
            yoyChangeDto.setDraftCount(calculateDifference(current.getDraftCount(), lastYear != null ? lastYear.getDraftCount() : null)); // 초안
            yoyChangeDto.setFinalCount(calculateDifference(current.getFinalCount(), lastYear != null ? lastYear.getFinalCount() : null)); // 최종
            yoyChangeDto.setDraftAverageDate(calculateDifference(current.getDraftAverageDate(), lastYear != null ? lastYear.getDraftAverageDate() : null)); // 초안평균일
            yoyChangeDto.setFinalAverageDate(calculateDifference(current.getFinalAverageDate(), lastYear != null ? lastYear.getFinalAverageDate() : null)); // 최종평균일
            yoyChangeDto.setVisitor(calculateDifference(current.getVisitor(), lastYear != null ? lastYear.getVisitor() : null)); // 방문자
            yoyChangeDto.setDailyVisitorAverage(calculateDifference(current.getDailyVisitorAverage(), previous != null ? previous.getDailyVisitorAverage() : null)); // 일일 방문자 평균
        }
        
        // 세팅
        periodDataMap.put("MOM_CHANGE", momChangeDto);
        periodDataMap.put("YOY_CHANGE", yoyChangeDto);

        return periodDataMap;
    }

    /**
     * 두 값의 차이를 계산하고 포맷팅하여 반환합니다.
     * 필드의 데이터가 없을 경우 0으로 간주합니다.
     * @param currentStr 현재 값 문자열
     * @param previousStr 이전 값 문자열 (MoM 또는 YoY)
     * @return 포맷팅된 차이 문자열 (예: "+5", "-3", "0", "-")
     */
    private String calculateDifference(String currentStr, String previousStr) {
        BigDecimal current = BigDecimal.ZERO;
        if (currentStr != null && !currentStr.isEmpty() && !currentStr.equals("-") && !currentStr.equals("N/A") && !currentStr.equals("Error")) {
            try {
                current = new BigDecimal(currentStr);
            } catch (NumberFormatException e) {
                return "Error";
            }
        }

        BigDecimal previous = BigDecimal.ZERO;
        if (previousStr != null && !previousStr.isEmpty() && !previousStr.equals("-") && !previousStr.equals("N/A") && !previousStr.equals("Error")) {
            try {
                previous = new BigDecimal(previousStr);
            } catch (NumberFormatException e) {
                return "Error";
            }
        }

        BigDecimal difference = current.subtract(previous);

        String sign = difference.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            sign = "";
        }

        // 부호 포함 리턴
        return sign + difference.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
	
    // 엑셀 export
	@Override
	public ExcelExportResult admin702ExportExcel(Admin702ExcelDTO params) throws IOException{
        // 데이터 조회 및 병합
        List<MonthlyQnaStatisticsDataDTO> monthlyQnaData = admin702Mapper.selectMonthlyQnaStatisticsDataForExcel(params);
		List<MonthlyVisitorStatisticsDataDTO> monthlyVisitorData = admin702Mapper.selectMonthlyVisitorStatisticsDataForExcel(params);
        Map<String, MonthlyStatisticsDataDTO> mergedDataMap = this.mergeStatisticsData(monthlyQnaData, monthlyVisitorData);
        List<MonthlyStatisticsDataDTO> excelExportList = new ArrayList<>(mergedDataMap.values());
        
        // 순서대로 출력하기 위해 정렬
        excelExportList.sort(Comparator.comparing(dto -> {
            switch (dto.getPeriodType() != null ? dto.getPeriodType() : dto.getMonthGrp()) {
                case "YEARLY": return 1; // 년 누적
                case "CURRENT": return 2; // 현재월
                case "PREVIOUS": return 3; // 이전월
                case "MOM_CHANGE": return 4; // 전월대비 증감
                case "LAST_YEAR": return 5; // 전년동월
                case "YOY_CHANGE": return 6; // 전년동월대비 증감
                default: return 7;
            }
        }));

	    String pageId = "admin702";
		
	    byte[] bytes = excelComponent.excelExportByPage(pageId, excelExportList);
	    
	    ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
	    
	    String title = pageInfo.getTitle();
	    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	    String fileName = title + "_" + date + ".xlsx";
	    
		return new ExcelExportResult(fileName, bytes);
	}
}