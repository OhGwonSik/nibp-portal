package egovframework.admin.admin800.controller;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin806VO;
import egovframework.admin.admin800.domain.Admin806filterDto;
import egovframework.admin.admin800.service.Admin806Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin806")
public class Admin806Controller {
	private final Admin806Service admin806Service;

	@GetMapping("/personal-info-proc-log/list")
	public ResponseEntity<ApiResponse<PageInfo<Admin806VO>>> selectPersonalInfoProcLogWithFilter(@LogParam @ModelAttribute Admin806filterDto filter) {
		return ResponseEntity.ok(ApiResponse.success(admin806Service.selectPersonalInfoProcLogWithFilter(filter)));
	}

	@PostMapping("/personal-info-proc-log/excel/export")
	@CheckMenuPermission(permission = PermissionType.EXCEL)
	public ResponseEntity<byte[]> admin806ExportExcel(@LogParam @RequestBody @Valid Admin806filterDto filter) throws IOException {
		if (filter.getReason() == null || filter.getReason().trim().length() < 2) {
			throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
		}

		ExcelExportResult result = admin806Service.admin806ExportExcel(filter);

		String encodedFileName = URLEncoder.encode(result.getFileName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
		String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(result.getBytes());
	}
}
