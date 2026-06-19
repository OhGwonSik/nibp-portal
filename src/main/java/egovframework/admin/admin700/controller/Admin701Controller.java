package egovframework.admin.admin700.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin700.domain.Admin701ExcelDTO;
import egovframework.admin.admin700.domain.Admin701FilterDTO;
import egovframework.admin.admin700.domain.QnaDataDTO;
import egovframework.admin.admin700.service.Admin701Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin701")
public class Admin701Controller {
	private final Admin701Service Admin701Service;
	
	@GetMapping("/list")
    @CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<PageInfo<QnaDataDTO>>> selectQnaDataList(@LogParam @ModelAttribute Admin701FilterDTO filter) {
		PageInfo<QnaDataDTO> result = Admin701Service.selectQnaDataList(filter);
        return ResponseEntity.ok(ApiResponse.success(result));
	}

	@PostMapping("/excel/export")
    @CheckMenuPermission(permission = PermissionType.EXCEL)
    public ResponseEntity<byte[]> admin701ExportExcel(@LogParam @RequestBody Admin701ExcelDTO params) throws IOException {
        if(params.getReason() == null || params.getReason().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }

        ExcelExportResult result = Admin701Service.admin701ExportExcel(params);
        
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
