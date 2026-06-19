package egovframework.common.excel.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@EnableConfigurationProperties(ExcelConfig.class)
@RequiredArgsConstructor
@Slf4j
public class ExcelComponent {
	private final ExcelConfig excelConfig;
	private final ResourceLoader resourceLoader;
    @Value("${app.excel-template-path}")
    private String excelTemplatePath;
	
	private final ObjectMapper mapper = new ObjectMapper()
	        .registerModule(new JavaTimeModule())
	        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //ObjectMapper에 JavaTimeModule 등록
	
	/**
	 * 페이지 기반 Excel 파일 생성 (공통)
	 *
	 * excel-download-config.properties 에 정의된 페이지 설정(pageId)을 기반으로
	 * JXLS 템플릿을 사용하여 동적으로 헤더/필드를 구성하고 Excel 파일을 생성한다.
	 *
	 * @param pageId
	 * @param dataList 조회 결과 데이터 목록 (List<T>)
	 * @return byte[] 생성된 Excel 파일 (다운로드용)
	 * @throws Exception Excel 처리 중 오류 발생 시
	 */
    public <T> byte[] excelExportByPage(String pageId, List<T> dataList) throws IOException {
        log.info("excelConfig=>{}",excelConfig);
        
		if(dataList.isEmpty()) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "다운로드 할 데이터가 없습니다.");
		}
		
		ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
		
        if (pageInfo == null) {
        	throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "엑셀 설정을 찾을 수 없습니다. pageId:" + pageId);
        }
        
        List<Map<String, String>> headerConvertMap = pageInfo.getHeader().stream()
                .map(h -> Map.of(
                        "label", h.getLabel(),
                        "field", h.getField()
                ))
                .toList();
        log.info("headerMap=>{}",headerConvertMap);
        List<String> fields = headerConvertMap.stream()
                .map(h -> h.get("field"))
                .toList();
        
        String templatePath = "classpath:" + excelConfig.getTemplate();
        Resource template = resourceLoader.getResource(templatePath);
        if (!template.exists()) {
        	throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "템플릿 파일을 찾을 수 없습니다: " + templatePath);
        }

        List<Map<String, Object>> normalizedList = normalizeDataList(dataList);

        String userNmKorn = SecurityUtil.getUser().getUserNmKorn();
        String reportDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        try (InputStream is = template.getInputStream();
        		ByteArrayOutputStream os = new ByteArrayOutputStream()) {

				Context context = new Context();
				context.putVar("reportTitle", pageInfo.getTitle());
				context.putVar("headers", headerConvertMap);
				context.putVar("fields", fields);
				context.putVar("list", normalizedList);
				context.putVar("userNmKorn", userNmKorn);
				context.putVar("reportDate", reportDate);

				// JXLS 최적화 실행
	            JxlsHelper jxlsHelper = JxlsHelper.getInstance();
	            // 수식 계산 프로세서 끔
	            jxlsHelper.setProcessFormulas(false);
	            jxlsHelper.setUseFastFormulaProcessor(true);

	            jxlsHelper.processTemplate(is, os, context);

		        return os.toByteArray();
        }
    }

    	/**
	 * 특정 템플릿 기반 로딩
	 *
	 *
	 * @param pageId
	 * @param dataList 조회 결과 데이터 목록 (List<T>)
	 * @return byte[] 생성된 Excel 파일 (다운로드용)
	 * @throws Exception Excel 처리 중 오류 발생 시
	 */
    public <T> byte[] excelExportByTemplateName(String templateName, List<T> dataList) throws IOException {
		log.info("===== Excel Template Processing =====");
		log.info("Template name: {}", templateName);
		log.info("Data list size: {}", dataList.size());
		if(!dataList.isEmpty()) {
			log.info("Data list type: {}", dataList.get(0).getClass().getName());
		}

		if(dataList.isEmpty()) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "다운로드 할 데이터가 없습니다.");
		}

		String fullPath = excelTemplatePath + templateName;
        Resource template = resourceLoader.getResource(fullPath);
        if (!template.exists()) {
        	throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "템플릿 파일을 찾을 수 없습니다: " + fullPath);
        }

        try (InputStream is = template.getInputStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            Context context = new Context();
            // 테스트: 원본 DTO를 직접 넘기기 (Map 변환 없이)
            context.putVar("list", normalizeDataList(dataList));

            log.info("===== JXLS Processing =====");
            log.info("List size: {}", dataList.size());
            if(!dataList.isEmpty()) {
                log.info("List item type: {}", dataList.get(0).getClass().getName());
                log.info("First item: {}", dataList.get(0));
            }
            log.info("Context vars: {}", context.toMap().keySet());

            try {
                JxlsHelper jxlsHelper = JxlsHelper.getInstance();
                jxlsHelper.setUseFastFormulaProcessor(false);
                jxlsHelper.processTemplate(is, os, context);
                log.info("JXLS processing completed successfully");
            } catch (Exception e) {
                log.error("JXLS processing failed", e);
                throw e;
            }

            log.info("===========================");

			byte[] result = os.toByteArray();

            return result;
        }
    }
    
    /**
     * DTO/VO → Map<String, Object> 변환
     * JXLS 동적 필드 접근을 위해 반드시 필요
     */
    private Map<String, Object> convertToMap(Object obj) {
        try {
            return mapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Excel 데이터 변환 오류", e);
        }
    }
    
    /**
     * List<T> → List<Map<String, Object>> 변환
     * T가 Map이면 그대로 두고
     * T가 DTO/VO면 convertValue로 Map으로 변환
     */
    @SuppressWarnings("unchecked")    
    private List<Map<String, Object>> normalizeDataList(List<?> list) {
        return list.stream()
                .map(item -> {
                    if (item instanceof Map<?, ?> map) {
                        // 이미 Map이면 그대로 (캐스팅만)
                        return (Map<String, Object>) map;
                    } else {
                        // DTO/VO → Map으로 변환
                        return convertToMap(item);
                    }
                })
                .toList();
    }
    
    /**
     * 출석부 엑셀 생성 (동적 교육일 헤더, 드롭다운 유효성 검사, 시트 보호 포함)
     * 
     * @param attendanceList 출석 데이터 목록
     * @return byte[] 생성된 Excel 파일
     * @throws IOException Excel 생성 중 오류 발생 시
     */
    public byte[] createAttendanceExcel(List<?> attendanceList) throws IOException {
        log.info("===== Attendance Excel Creation =====");
        log.info("Attendance list size: {}", attendanceList.size());
        
        if (attendanceList.isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "다운로드 할 데이터가 없습니다.");
        }
        
        // 출력자 정보 (현재 로그인 사용자)
        String userName = SecurityUtil.getUser() != null ? 
                SecurityUtil.getUser().getUserNmKorn() : "관리자";
        
        // 데이터 정규화
        List<Map<String, Object>> dataList = normalizeDataList(attendanceList);
        
        // Workbook 생성
        Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        Sheet sheet = workbook.createSheet("출석부");
        
        // 스타일 생성
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle mergedHeaderStyle = createMergedHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        CellStyle dateHeaderStyle = createDateHeaderStyle(workbook);
        
        // 교육일 목록 추출 (동적)
        List<String> eduDateList = extractEduDates(dataList);
        log.info("교육일 목록: {}", eduDateList);
        
        // 제목 및 출력자/출력일자 (1~3행)
        createTitleRows(sheet, workbook, eduDateList.size(), userName);
        
        // 헤더 생성 (4~5행)
        createHeaders(sheet, headerStyle, mergedHeaderStyle, dateHeaderStyle, eduDateList);
        
        // 데이터 생성 (6행부터)
        int rowNum = createDataRows(sheet, dataStyle, centerStyle, dataList, eduDateList);
        
        // 열 너비 자동 조정
        adjustColumnWidths(sheet, eduDateList.size());
        
        // 출석 여부 유효성 검사 설정 (Y/N만 입력 가능)
        addAttendanceValidation(sheet, rowNum, eduDateList.size());
        
        // 시트 보호 (교육일 컬럼만 수정 가능)
        protectSheet(sheet, rowNum, eduDateList.size(), centerStyle);
        
        // ByteArrayOutputStream으로 변환
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            workbook.write(os);
            workbook.close();
            return os.toByteArray();
        }
    }
    
    /**
     * 헤더 스타일 생성 (연한 파랑색 - 기존 양식과 동일)
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        // RGB(217, 225, 242) - 연한 파랑색
        ((org.apache.poi.xssf.usermodel.XSSFCellStyle) style).setFillForegroundColor(
            new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte)217, (byte)225, (byte)242}, null)
        );
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    /**
     * 병합된 헤더 스타일 생성 (교육일) - 동일한 연한 파랑색
     */
    private CellStyle createMergedHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        // RGB(217, 225, 242) - 연한 파랑색
        ((org.apache.poi.xssf.usermodel.XSSFCellStyle) style).setFillForegroundColor(
            new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte)217, (byte)225, (byte)242}, null)
        );
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    /**
     * 날짜 헤더 스타일 생성 - 동일한 연한 파랑색
     */
    private CellStyle createDateHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        // RGB(217, 225, 242) - 연한 파랑색
        ((org.apache.poi.xssf.usermodel.XSSFCellStyle) style).setFillForegroundColor(
            new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte)217, (byte)225, (byte)242}, null)
        );
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    /**
     * 데이터 스타일 생성
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 중앙 정렬 스타일 생성
     */
    private CellStyle createCenterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 교육일 목록 추출
     */
    private List<String> extractEduDates(List<Map<String, Object>> dataList) {
        if (dataList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        // 첫 번째 데이터에서 교육일 컬럼 추출 (eduDate1, eduDate2, eduDate3, ...)
        Map<String, Object> firstRow = dataList.get(0);
        List<String> dates = new java.util.ArrayList<>();
        
        int index = 1;
        while (true) {
            String key = "eduDate" + index;
            if (firstRow.containsKey(key) && firstRow.get(key) != null) {
                dates.add(String.valueOf(firstRow.get(key)));
                index++;
            } else {
                break;
            }
        }
        
        return dates;
    }
    
    /**
     * 제목 행 생성 (출력자/출력일자 우측 상단 표시)
     */
    private void createTitleRows(Sheet sheet, Workbook workbook, int eduDateCount, String userName) {
        // 제목 스타일
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 우측 정렬 스타일
        CellStyle rightStyle = workbook.createCellStyle();
        Font rightFont = workbook.createFont();
        rightFont.setFontHeightInPoints((short) 10);
        rightStyle.setFont(rightFont);
        rightStyle.setAlignment(HorizontalAlignment.RIGHT);
        rightStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 1행: 출력자
        Row row1 = sheet.createRow(0);
        row1.setHeightInPoints(18);
        int lastCol = 4 + eduDateCount; // 번호, 구분, 회원아이디, 신청자명, 소속기관, 교육일들
        Cell userCell = row1.createCell(lastCol);
        userCell.setCellValue("출력자: " + userName);
        userCell.setCellStyle(rightStyle);
        
        // 2행: 제목 + 출력일자
        Row row2 = sheet.createRow(1);
        row2.setHeightInPoints(25);
        
        // 제목 (병합)
        Cell titleCell = row2.createCell(0);
        titleCell.setCellValue("출석부");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, lastCol - 1));
        
        // 출력일자 (우측)
        Cell dateCell = row2.createCell(lastCol);
        String currentDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("출력일자: yyyy-MM-dd HH:mm:ss"));
        dateCell.setCellValue(currentDate);
        dateCell.setCellStyle(rightStyle);
        
        // 3행: 빈 행
        sheet.createRow(2);
    }
    
    /**
     * 헤더 생성 (병합된 셀에도 모든 테두리 적용)
     */
    private void createHeaders(Sheet sheet, CellStyle headerStyle,
                               CellStyle mergedHeaderStyle,
                               CellStyle dateHeaderStyle,
                               List<String> eduDateList) {
        // 4행: 병합된 헤더
        Row mergedHeaderRow = sheet.createRow(3);
        mergedHeaderRow.setHeightInPoints(20);
        
        String[] fixedHeaders = {"번호", "구분", "회원아이디", "신청자명", "소속기관"};
        int colIndex = 0;
        
        // 고정 헤더 (4~5행 병합)
        for (String header : fixedHeaders) {
            // 4행 셀
            Cell cell4 = mergedHeaderRow.createCell(colIndex);
            cell4.setCellValue(header);
            cell4.setCellStyle(headerStyle);
            
            // 5행 셀도 생성 (병합되지만 테두리를 위해)
            Row dateHeaderRow = sheet.getRow(4);
            if (dateHeaderRow == null) {
                dateHeaderRow = sheet.createRow(4);
                dateHeaderRow.setHeightInPoints(18);
            }
            Cell cell5 = dateHeaderRow.createCell(colIndex);
            cell5.setCellStyle(headerStyle);
            
            // 4~5행 병합
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(3, 4, colIndex, colIndex));
            colIndex++;
        }
        
        // 교육일 병합 헤더
        if (!eduDateList.isEmpty()) {
            // 4행 교육일 병합 헤더 - 모든 셀 생성
            for (int i = 0; i < eduDateList.size(); i++) {
                Cell eduCell = mergedHeaderRow.createCell(colIndex + i);
                if (i == 0) {
                    eduCell.setCellValue("교육일");
                }
                eduCell.setCellStyle(mergedHeaderStyle);
            }
            
            // 병합 영역 설정
            if (eduDateList.size() > 1) {
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                        3, 3, colIndex, colIndex + eduDateList.size() - 1));
            }
            
            // 5행: 실제 날짜
            Row dateHeaderRow = sheet.getRow(4);
            if (dateHeaderRow == null) {
                dateHeaderRow = sheet.createRow(4);
                dateHeaderRow.setHeightInPoints(18);
            }
            for (int i = 0; i < eduDateList.size(); i++) {
                Cell dateCell = dateHeaderRow.createCell(colIndex + i);
                dateCell.setCellValue(eduDateList.get(i));
                dateCell.setCellStyle(dateHeaderStyle);
            }
        }
    }
    
    /**
     * 데이터 행 생성 (모든 셀에 테두리 적용)
     */
    private int createDataRows(Sheet sheet, CellStyle dataStyle,
                               CellStyle centerStyle,
                               List<Map<String, Object>> dataList,
                               List<String> eduDateList) {
        int rowNum = 5; // 6행부터 시작
        
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> data = dataList.get(i);
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(18);
            
            int colIndex = 0;
            
            // 번호
            Cell numCell = row.createCell(colIndex++);
            numCell.setCellValue(i + 1);
            numCell.setCellStyle(centerStyle);
            
            // 구분
            Cell typeCell = row.createCell(colIndex++);
            typeCell.setCellValue(getStringValue(data, "userType"));
            typeCell.setCellStyle(centerStyle);
            
            // 회원아이디
            Cell userIdCell = row.createCell(colIndex++);
            userIdCell.setCellValue(getStringValue(data, "userId"));
            userIdCell.setCellStyle(dataStyle);
            
            // 신청자명
            Cell nameCell = row.createCell(colIndex++);
            nameCell.setCellValue(getStringValue(data, "userName"));
            nameCell.setCellStyle(dataStyle);
            
            // 소속기관
            Cell orgCell = row.createCell(colIndex++);
            orgCell.setCellValue(getStringValue(data, "orgNm"));
            orgCell.setCellStyle(dataStyle);
            
            // 교육일별 출석 여부 (빈 셀도 생성하여 테두리 적용)
            for (int j = 0; j < eduDateList.size(); j++) {
                Cell attendCell = row.createCell(colIndex++);
                String attendKey = "attend" + (j + 1);
                String attendValue = getStringValue(data, attendKey);
                attendCell.setCellValue(attendValue);
                attendCell.setCellStyle(centerStyle);
            }
        }
        
        return rowNum;
    }
    
    /**
     * 열 너비 조정 (수료처리 제거)
     */
    private void adjustColumnWidths(Sheet sheet, int eduDateCount) {
        sheet.setColumnWidth(0, 2000);  // 번호 (좁게)
        sheet.setColumnWidth(1, 3000);  // 구분
        sheet.setColumnWidth(2, 4500);  // 회원아이디
        sheet.setColumnWidth(3, 4000);  // 신청자명
        sheet.setColumnWidth(4, 6000);  // 소속기관
        
        // 교육일 컬럼들
        for (int i = 0; i < eduDateCount; i++) {
            sheet.setColumnWidth(5 + i, 4000);
        }
    }
    
    /**
     * 출석 여부 데이터 유효성 검사 (출석/결석만 입력 가능, 드롭다운 없음)
     */
    private void addAttendanceValidation(Sheet sheet, int lastRowNum, int eduDateCount) {
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint =
                validationHelper.createExplicitListConstraint(new String[]{"출석", "결석"});
        
        // 교육일 컬럼들에 대해 유효성 검사 설정
        for (int i = 0; i < eduDateCount; i++) {
            int colIndex = 5 + i; // 교육일 시작 컬럼
            org.apache.poi.ss.util.CellRangeAddressList addressList = 
                    new org.apache.poi.ss.util.CellRangeAddressList(5, lastRowNum - 1, colIndex, colIndex);
            DataValidation validation =
                    validationHelper.createValidation(constraint, addressList);
            
            // 유효성 검사 설정 (드롭다운 화살표 숨김)
            validation.setEmptyCellAllowed(true); // 빈 값 허용
            validation.setShowErrorBox(true);
            validation.setSuppressDropDownArrow(true); // 드롭다운 화살표 숨김
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox("입력 오류", "출석,결석만 입력 가능합니다.");
            
            sheet.addValidationData(validation);
        }
    }
    
    /**
     * 시트 보호 설정 (교육일 컬럼만 수정 가능)
     */
    private void protectSheet(Sheet sheet, int lastRowNum, int eduDateCount, 
                             CellStyle centerStyle) {
        Workbook workbook = sheet.getWorkbook();
        
        // 잠금 해제된 스타일 생성 (교육일 셀용)
        CellStyle unlockedStyle = workbook.createCellStyle();
        unlockedStyle.cloneStyleFrom(centerStyle);
        unlockedStyle.setLocked(false); // 잠금 해제
        unlockedStyle.setAlignment(HorizontalAlignment.CENTER);
        unlockedStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        unlockedStyle.setBorderTop(BorderStyle.THIN);
        unlockedStyle.setBorderBottom(BorderStyle.THIN);
        unlockedStyle.setBorderLeft(BorderStyle.THIN);
        unlockedStyle.setBorderRight(BorderStyle.THIN);
        
        // 데이터 행 (6행부터)의 교육일 셀만 잠금 해제
        for (int rowIdx = 5; rowIdx < lastRowNum; rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row == null) continue;
            
            // 교육일 컬럼만 잠금 해제
            for (int colIdx = 5; colIdx < 5 + eduDateCount; colIdx++) {
                Cell cell = row.getCell(colIdx);
                if (cell != null) {
                    cell.setCellStyle(unlockedStyle);
                }
            }
        }
        
        // 시트 보호 활성화 (비밀번호 없음)
        sheet.protectSheet("");
    }
    
    /**
     * Map에서 String 값 가져오기
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
    
    /**
     * 출석부 엑셀 파일 파싱
     * 
     * @param file 업로드된 엑셀 파일
     * @return List<Map<String, Object>> 파싱된 데이터
     * @throws IOException 파일 읽기 실패 시
     */
    public List<Map<String, Object>> parseAttendanceExcel(MultipartFile file) throws IOException {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // 헤더 행 파싱 (4행: 병합된 헤더, 5행: 교육일 날짜)
            Row headerRow1 = sheet.getRow(3); // 4행 (0-based index)
            Row headerRow2 = sheet.getRow(4); // 5행
            
            if (headerRow1 == null || headerRow2 == null) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "엑셀 헤더가 올바르지 않습니다.");
            }
            
            // 교육일 컬럼 개수 파악
            int eduDateStartCol = 5; // F열부터
            List<String> eduDates = new java.util.ArrayList<>();
            
            for (int colIdx = eduDateStartCol; colIdx < headerRow2.getLastCellNum(); colIdx++) {
                Cell cell = headerRow2.getCell(colIdx);
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    String dateValue = cell.getStringCellValue();
                    if (dateValue != null && !dateValue.trim().isEmpty()) {
                        eduDates.add(dateValue.trim());
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
            
            // 데이터 행 파싱 (6행부터)
            for (int rowIdx = 5; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;
                
                // 첫 번째 셀(번호)이 비어있으면 skip
                Cell firstCell = row.getCell(0);
                if (firstCell == null || firstCell.getCellType() == CellType.BLANK) {
                    continue;
                }
                
                Map<String, Object> rowData = new java.util.HashMap<>();
                
                // 번호 (A열, index 0) - skip
                // 구분 (B열, index 1)
                rowData.put("userType", getCellValueAsString(row.getCell(1)));
                
                // 회원아이디 (C열, index 2)
                rowData.put("userId", getCellValueAsString(row.getCell(2)));
                
                // 신청자명 (D열, index 3)
                rowData.put("userName", getCellValueAsString(row.getCell(3)));
                
                // 소속기관 (E열, index 4)
                rowData.put("orgNm", getCellValueAsString(row.getCell(4)));
                
                // 교육일별 출석 여부 (F열부터)
                for (int i = 0; i < eduDates.size(); i++) {
                    String attendKey = "attend" + (i + 1);
                    String attendValue = getCellValueAsString(row.getCell(eduDateStartCol + i));
                    rowData.put(attendKey, attendValue);
                    rowData.put("eduDate" + (i + 1), eduDates.get(i));
                }
                
                result.add(rowData);
            }
        }
        return result;
    }
    
    /**
     * 셀 값을 String으로 변환
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 만족도 통계 엑셀 생성 (피벗 형태 - 화면과 동일한 구조)
     *
     * @param dataList 월별 통계 데이터 목록
     * @param targetYear 대상 연도
     * @return byte[] 생성된 Excel 파일
     * @throws IOException Excel 생성 중 오류 발생 시
     */
    public byte[] createSatisfactionStatisticsExcel(List<Map<String, Object>> dataList, String targetYear) throws IOException {
        log.info("===== Satisfaction Statistics Excel Creation =====");
        log.info("Data list size: {}", dataList.size());
        log.info("Target year: {}", targetYear);

        // 출력자 정보 (현재 로그인 사용자)
        String userName = SecurityUtil.getUser() != null ?
                SecurityUtil.getUser().getUserNmKorn() : "관리자";

        // Workbook 생성
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("만족도 통계");

        // 스타일 생성
        CellStyle titleStyle = createSatisfactionTitleStyle(workbook);
        CellStyle infoStyle = createSatisfactionInfoStyle(workbook);
        CellStyle headerStyle = createSatisfactionHeaderStyle(workbook);
        CellStyle totalHeaderStyle = createSatisfactionTotalHeaderStyle(workbook);
        CellStyle categoryStyle = createSatisfactionCategoryStyle(workbook);
        CellStyle totalCategoryStyle = createSatisfactionTotalCategoryStyle(workbook);
        CellStyle dataStyle = createSatisfactionDataStyle(workbook);
        CellStyle totalDataStyle = createSatisfactionTotalDataStyle(workbook);

        // 연도 프리픽스 (예: "25년")
        String yearPrefix = targetYear.substring(2) + "년 ";

        // 데이터 피벗 변환
        Map<String, Map<String, Object>> pivotedData = pivotSatisfactionData(dataList);

        // 행 카테고리 정의 (화면과 동일한 순서)
        Map<String, String> rowCategories = new LinkedHashMap<>();
        rowCategories.put("publicPost", "공개");
        rowCategories.put("privatePost", "비공개");
        rowCategories.put("satisfied", "만족");
        rowCategories.put("unsatisfied", "불만족");
        rowCategories.put("noResponse", "미응답");
        rowCategories.put("total", "합계");
        rowCategories.put("publicPostRate", "공개율(%)");
        rowCategories.put("privatePostRate", "비공개율(%)");

        int rowNum = 0;

        // 1행: 출력자
        Row row1 = sheet.createRow(rowNum++);
        row1.setHeightInPoints(18);
        Cell userCell = row1.createCell(13); // 마지막 열
        userCell.setCellValue("출력자: " + userName);
        userCell.setCellStyle(infoStyle);

        // 2행: 제목
        Row row2 = sheet.createRow(rowNum++);
        row2.setHeightInPoints(25);
        Cell titleCell = row2.createCell(0);
        titleCell.setCellValue("만족도 통계");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 12));

        // 출력일자
        Cell dateCell = row2.createCell(13);
        String currentDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("출력일자: yyyy-MM-dd HH:mm:ss"));
        dateCell.setCellValue(currentDate);
        dateCell.setCellStyle(infoStyle);

        // 3행: 빈 행
        sheet.createRow(rowNum++);

        // 4행: 헤더
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.setHeightInPoints(22);

        // 구분 헤더
        Cell guBunHeader = headerRow.createCell(0);
        guBunHeader.setCellValue("구분");
        guBunHeader.setCellStyle(headerStyle);

        // 월별 헤더 (1월 ~ 12월)
        for (int i = 1; i <= 12; i++) {
            Cell monthHeader = headerRow.createCell(i);
            monthHeader.setCellValue(yearPrefix + i + "월");
            monthHeader.setCellStyle(headerStyle);
        }

        // 합계 헤더
        Cell totalHeader = headerRow.createCell(13);
        totalHeader.setCellValue("합계");
        totalHeader.setCellStyle(totalHeaderStyle);

        // 데이터 행 생성
        for (Map.Entry<String, String> category : rowCategories.entrySet()) {
            String catKey = category.getKey();
            String catLabel = category.getValue();
            boolean isTotal = catKey.equals("total");
            boolean isRate = catKey.endsWith("Rate");

            Row dataRow = sheet.createRow(rowNum++);
            dataRow.setHeightInPoints(20);

            // 구분 열
            Cell catCell = dataRow.createCell(0);
            catCell.setCellValue(catLabel);
            catCell.setCellStyle(isTotal ? totalCategoryStyle : categoryStyle);

            Map<String, Object> rowData = pivotedData.getOrDefault(catKey, new LinkedHashMap<>());

            // 월별 데이터 (1월 ~ 12월)
            for (int i = 1; i <= 12; i++) {
                Cell dataCell = dataRow.createCell(i);
                String monthKey = String.format("%02d", i);
                Object value = rowData.get(monthKey);

                String displayValue = formatCellValue(value, isRate);
                dataCell.setCellValue(displayValue);
                dataCell.setCellStyle(isTotal ? totalDataStyle : dataStyle);
            }

            // 합계 열
            Cell totalCell = dataRow.createCell(13);
            Object totalValue = rowData.get("total");
            String displayTotalValue = formatCellValue(totalValue, isRate);
            totalCell.setCellValue(displayTotalValue);
            totalCell.setCellStyle(totalDataStyle);
        }

        // 열 너비 설정
        sheet.setColumnWidth(0, 4000);  // 구분
        for (int i = 1; i <= 12; i++) {
            sheet.setColumnWidth(i, 3000);  // 월별
        }
        sheet.setColumnWidth(13, 3000);  // 합계

        // ByteArrayOutputStream으로 변환
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            workbook.write(os);
            workbook.close();
            return os.toByteArray();
        }
    }

    /**
     * 만족도 통계 데이터 피벗 변환
     * DB에서 월별 row로 오는 데이터를 카테고리별 row로 변환
     */
    private Map<String, Map<String, Object>> pivotSatisfactionData(List<Map<String, Object>> dataList) {
        Map<String, Map<String, Object>> pivotedData = new LinkedHashMap<>();

        // 카테고리 초기화
        String[] categories = {"publicPost", "privatePost", "satisfied", "unsatisfied",
                               "noResponse", "total", "publicPostRate", "privatePostRate"};
        for (String cat : categories) {
            pivotedData.put(cat, new LinkedHashMap<>());
        }

        // 전체 누적 데이터 찾기
        Map<String, Object> totalData = null;
        for (Map<String, Object> item : dataList) {
            String monthGrp = String.valueOf(item.get("monthGrp"));
            if ("(전체누적)".equals(monthGrp)) {
                totalData = item;
                break;
            }
        }

        // 전체 누적 데이터 설정
        if (totalData != null) {
            for (String cat : categories) {
                Object value = totalData.get(cat);
                pivotedData.get(cat).put("total", value != null ? value : 0);
            }
        }

        // 월별 데이터 매핑
        for (Map<String, Object> item : dataList) {
            String monthGrp = String.valueOf(item.get("monthGrp"));
            if (!"(전체누적)".equals(monthGrp) && monthGrp.contains("-")) {
                String[] parts = monthGrp.split("-");
                if (parts.length == 2) {
                    String monthKey = parts[1]; // "01", "02", ... "12"

                    for (String cat : categories) {
                        Object value = item.get(cat);
                        pivotedData.get(cat).put(monthKey, value != null ? value : 0);
                    }
                }
            }
        }

        return pivotedData;
    }

    /**
     * 셀 값 포맷팅
     */
    private String formatCellValue(Object value, boolean isRate) {
        if (value == null) {
            return "0";
        }

        String strValue = String.valueOf(value);
        if (isRate && !"-".equals(strValue)) {
            // 소수점 처리
            try {
                double numValue = Double.parseDouble(strValue);
                if (numValue == (long) numValue) {
                    return String.format("%.0f%%", numValue);
                } else {
                    return String.format("%.1f%%", numValue);
                }
            } catch (NumberFormatException e) {
                return strValue + "%";
            }
        }
        return strValue;
    }

    /**
     * 만족도 통계 제목 스타일
     */
    private CellStyle createSatisfactionTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 만족도 통계 정보 스타일 (출력자, 출력일자)
     */
    private CellStyle createSatisfactionInfoStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 만족도 통계 헤더 스타일 (파란색 배경)
     */
    private CellStyle createSatisfactionHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        // 파란색 배경 (RGB: 68, 114, 196)
        ((XSSFCellStyle) style).setFillForegroundColor(
            new XSSFColor(new byte[]{(byte)68, (byte)114, (byte)196}, null)
        );
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 만족도 통계 합계 헤더 스타일 (빨간색 배경)
     */
    private CellStyle createSatisfactionTotalHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        // 빨간색 배경 (RGB: 192, 80, 77)
        ((XSSFCellStyle) style).setFillForegroundColor(
            new XSSFColor(new byte[]{(byte)192, (byte)80, (byte)77}, null)
        );
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 만족도 통계 카테고리 스타일 (파란색 배경)
     */
    private CellStyle createSatisfactionCategoryStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        // 파란색 배경 (RGB: 68, 114, 196)
        ((XSSFCellStyle) style).setFillForegroundColor(
            new XSSFColor(new byte[]{(byte)68, (byte)114, (byte)196}, null)
        );
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 만족도 통계 합계 카테고리 스타일 (빨간색 배경)
     */
    private CellStyle createSatisfactionTotalCategoryStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        // 빨간색 배경 (RGB: 192, 80, 77)
        ((XSSFCellStyle) style).setFillForegroundColor(
            new XSSFColor(new byte[]{(byte)192, (byte)80, (byte)77}, null)
        );
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 만족도 통계 데이터 스타일
     */
    private CellStyle createSatisfactionDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 만족도 통계 합계 데이터 스타일 (연한 빨간색 배경)
     */
    private CellStyle createSatisfactionTotalDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        // 연한 빨간색 배경 (RGB: 242, 220, 219)
        ((XSSFCellStyle) style).setFillForegroundColor(
            new XSSFColor(new byte[]{(byte)242, (byte)220, (byte)219}, null)
        );
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
