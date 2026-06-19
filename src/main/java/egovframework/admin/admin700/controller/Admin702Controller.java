package egovframework.admin.admin700.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.admin.admin700.domain.Admin702ExcelDTO;
import egovframework.admin.admin700.domain.Admin702FilterDTO;
import egovframework.admin.admin700.domain.MonthlyStatisticsDataDTO;
import egovframework.admin.admin700.service.Admin702Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin702")
public class Admin702Controller {
	private final Admin702Service admin702Service;
	
	@GetMapping("/monthly-qna-statistics")
    @CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<Map<String, MonthlyStatisticsDataDTO>>> selectMonthlyQnaStatisticsData(@LogParam @ModelAttribute Admin702FilterDTO filter) {
		Map<String, MonthlyStatisticsDataDTO> result = admin702Service.selectMonthlyQnaStatisticsData(filter);
        return ResponseEntity.ok(ApiResponse.success(result));
	}

	@PostMapping("/excel/export")
    @CheckMenuPermission(permission = PermissionType.EXCEL)
    public ResponseEntity<byte[]> admin702ExportExcel(@LogParam @RequestBody Admin702ExcelDTO params) throws IOException {
        if(params.getReason() == null || params.getReason().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }

        ExcelExportResult result = admin702Service.admin702ExportExcel(params);
        
        String fileName = result.getFileName();
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(result.getBytes());
    }
	
}