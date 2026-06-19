package egovframework.admin.admin700.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.admin.admin700.domain.QnaMonthlyResponseStatDTO;
import egovframework.admin.admin700.domain.QnaMonthlyResponseStatFilterDTO;
import egovframework.admin.admin700.service.Admin703Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin703/qna-stat")
public class Admin703Controller {

	private final Admin703Service Admin703Service;
	
	@GetMapping("/list")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<List<QnaMonthlyResponseStatDTO>>> selectQnaMonthlyResponseStat(QnaMonthlyResponseStatFilterDTO qnaMonthlyResponseStatFilterDTO) {
		List<QnaMonthlyResponseStatDTO> result = Admin703Service.selectQnaMonthlyResponseStat(qnaMonthlyResponseStatFilterDTO);
        return ResponseEntity.ok(ApiResponse.success(result));
	}
	
    @PostMapping("/excel/export")
    @CheckMenuPermission(permission = PermissionType.EXCEL)
    public ResponseEntity<byte[]> admin703ExportExcel(@LogParam @RequestBody QnaMonthlyResponseStatFilterDTO qnaMonthlyResponseStatFilterDTO) throws IOException {

        if(qnaMonthlyResponseStatFilterDTO.getReason() == null || qnaMonthlyResponseStatFilterDTO.getReason().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }

        ExcelExportResult result = Admin703Service.admin703ExportExcel(qnaMonthlyResponseStatFilterDTO);
        
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
