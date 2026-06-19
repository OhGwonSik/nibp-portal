package egovframework.admin.admin700.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import egovframework.admin.admin700.domain.Admin708ExcelDTO;
import egovframework.admin.admin700.domain.Admin708FilterDTO;
import egovframework.admin.admin700.domain.QnaSatisfactionStatistcsDTO;
import egovframework.admin.admin700.mapper.Admin708Mapper;
import egovframework.admin.admin700.service.Admin708Service;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Admin708ServiceImpl extends EgovAbstractServiceImpl implements Admin708Service {
    private final Admin708Mapper admin708Mapper;
    private final ExcelComponent excelComponent;
    private final ObjectMapper objectMapper;

    @Override
    public List<QnaSatisfactionStatistcsDTO> selectQnaSatisfactionStatisticsData(Admin708FilterDTO filter) {
        return admin708Mapper.selectQnaSatisfactionStatisticsData(filter);
    }

    @Override
    public ExcelExportResult admin708ExportExcel(Admin708ExcelDTO params) throws IOException {
        List<QnaSatisfactionStatistcsDTO> qnaStatisticsList = admin708Mapper.selectQnaSatisfactionStatisticsDataForExcel(params);
        if (qnaStatisticsList == null || qnaStatisticsList.isEmpty()) {
            throw new BusinessException(ErrorCode.EXCEL_DOWNLOAD_NO_DATA, "엑셀 데이터가 존재하지 않습니다.");
        }

        // DTO 리스트를 Map 리스트로 변환
        List<Map<String, Object>> dataList = qnaStatisticsList.stream()
                .map(dto -> objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {}))
                .toList();

        // 피벗 형태의 엑셀 생성
        byte[] bytes = excelComponent.createSatisfactionStatisticsExcel(dataList, params.getTargetYear());

        String title = "만족도통계";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = title + "_" + date + ".xlsx";

        return new ExcelExportResult(fileName, bytes);
    }
}
