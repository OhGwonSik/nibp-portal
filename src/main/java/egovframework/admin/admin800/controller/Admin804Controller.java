package egovframework.admin.admin800.controller;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin804filterDto;
import egovframework.admin.admin800.service.Admin804Service;
import egovframework.common.audit.domain.ApiAccessLog;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin/admin804")
@RequiredArgsConstructor
public class Admin804Controller {
    private final Admin804Service admin804Service;

    @GetMapping("/accesslog/list")
    public ResponseEntity<ApiResponse<PageInfo<ApiAccessLog>>> selectAccessLogWithFilter(@LogParam @ModelAttribute Admin804filterDto filter) {
        return ResponseEntity.ok(ApiResponse.success(admin804Service.selectAccessLogWithFilter(filter)));
    }

    @PostMapping("/accesslog/excel/export")
    @CheckMenuPermission(permission = PermissionType.EXCEL)
    public ResponseEntity<byte[]> admin804ExportExcel(@LogParam @RequestBody @Valid Admin804filterDto filter) throws IOException {
        if (filter.getReason() == null || filter.getReason().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }

        ExcelExportResult result = admin804Service.admin804ExportExcel(filter);

        String encodedFileName = URLEncoder.encode(result.getFileName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(result.getBytes());
    }
}