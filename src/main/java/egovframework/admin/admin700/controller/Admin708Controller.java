package egovframework.admin.admin700.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.admin.admin700.domain.Admin708ExcelDTO;
import egovframework.admin.admin700.domain.Admin708FilterDTO;
import egovframework.admin.admin700.domain.QnaSatisfactionStatistcsDTO;
import egovframework.admin.admin700.service.Admin708Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin708")
public class Admin708Controller {
	private final Admin708Service admin708Service;
	
	@GetMapping("/qna-satisfaction-statistics")
    @CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<List<QnaSatisfactionStatistcsDTO>>> selectQnaSatisfactionStatisticsData(@LogParam @ModelAttribute Admin708FilterDTO filter) {
		List<QnaSatisfactionStatistcsDTO> result = admin708Service.selectQnaSatisfactionStatisticsData(filter);
        return ResponseEntity.ok(ApiResponse.success(result));
	}

	@PostMapping("/excel/export")
    @CheckMenuPermission(permission = PermissionType.EXCEL)
    public ResponseEntity<byte[]> admin708ExportExcel(@LogParam @RequestBody Admin708ExcelDTO params) throws IOException {
        if(params.getReason() == null || params.getReason().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }

        ExcelExportResult result = admin708Service.admin708ExportExcel(params);
        
        String fileName = result.getFileName();
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(result.getBytes());
    }
}
