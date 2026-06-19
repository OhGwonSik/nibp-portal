package egovframework.admin.admin700.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.admin.admin700.domain.QnaCategoryStatDTO;
import egovframework.admin.admin700.domain.QnaCategoryStatFilterDTO;
import egovframework.admin.admin700.service.Admin707Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin707/qna-stat")
public class Admin707Controller {

	private final Admin707Service admin707Service;
	
	@GetMapping("/list")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<List<QnaCategoryStatDTO>>> selectQnaCategoryStat(QnaCategoryStatFilterDTO qnaCategoryStatFilterDTO) {
		List<QnaCategoryStatDTO> result = admin707Service.selectQnaCategoryStat(qnaCategoryStatFilterDTO);
        return ResponseEntity.ok(ApiResponse.success(result));
	}
	
    @PostMapping("/excel/export")
    @CheckMenuPermission(permission = PermissionType.EXCEL)
    public ResponseEntity<byte[]> admin707ExportExcel(@LogParam @RequestBody QnaCategoryStatFilterDTO qnaCategoryStatFilterDTO) throws IOException {

        if(qnaCategoryStatFilterDTO.getReason() == null || qnaCategoryStatFilterDTO.getReason().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }

        ExcelExportResult result = admin707Service.admin707ExportExcel(qnaCategoryStatFilterDTO);
        
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
