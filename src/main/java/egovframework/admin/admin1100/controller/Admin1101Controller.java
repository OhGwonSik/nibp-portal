package egovframework.admin.admin1100.controller;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin1100.domain.*;
import egovframework.admin.admin1100.service.Admin1101Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.component.EgovMapComponent;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @ClassName : Admin1101Controller.java
 * @Description : 정기발간자료 관리 Controller
 *
 * @author : j.h.kim
 * @since : 2025. 01. 12
 * @version : 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin1101")
@PreAuthorize("hasRole('ADMIN')")
public class Admin1101Controller {
    
    private final Admin1101Service admin1101Service;
    
    /**
     * 정기발간자료 목록 조회
     *
     * @param filter Admin1101FilterDTO
     * @return ResponseEntity<ApiResponse<PageInfo<Admin1101VO>>>
     */
    @PostMapping("/list/filter")
    @CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<PageInfo<Admin1101VO>>> selectAdmin1101List(
            @LogParam @Valid @RequestBody Admin1101FilterDTO filter) {
        PageInfo<Admin1101VO> response = admin1101Service.selectAdmin1101List(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 정기발간자료 상세 조회
     *
     * @param periodicalNo 정기발간자료 번호
     * @return ResponseEntity<ApiResponse<Admin1101DetailDTO>>
     */
    @GetMapping("/select")
    @CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<Admin1101DetailDTO>> selectAdmin1101Detail(
            @LogParam @RequestParam("fxtmPblsDataOid") Long fxtmPblsDataOid) {
        Admin1101DetailDTO response = admin1101Service.selectAdmin1101Detail(fxtmPblsDataOid);
        
        if (response == null) {
            return new ResponseEntity<>(
                ApiResponse.error(HttpStatus.NOT_FOUND, "정기발간자료 정보를 찾을 수 없습니다."),
                HttpStatus.NOT_FOUND
            );
        }
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 정기발간자료 등록
     *
     * @param dto Admin1101SaveDTO
     * @param user BaseUser
     * @return ResponseEntity<ApiResponse<Long>>
     */
    @PostMapping("/insert")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<Long>> insertAdmin1101(
            @LogParam @Valid @RequestBody Admin1101SaveDTO dto,
            @AuthenticationPrincipal BaseUser user) {
        Long fxtmPblsDataOid = admin1101Service.insertAdmin1101(dto, user.getUserId());
        return new ResponseEntity<>(ApiResponse.success(fxtmPblsDataOid), HttpStatus.OK);
    }
    
    /**
     * 정기발간자료 수정
     *
     * @param dto Admin1101SaveDTO
     * @param user BaseUser
     * @return ResponseEntity<ApiResponse<T>>
     */
    @PostMapping("/update")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<T>> updateAdmin1101(
            @LogParam @Valid @RequestBody Admin1101SaveDTO dto,
            @AuthenticationPrincipal BaseUser user) {
        admin1101Service.updateAdmin1101(dto, user.getUserId());
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
    }
    
    /**
     * 정기발간자료 삭제
     *
     * @param dto Admin1101DeleteDTO
     * @return ResponseEntity<ApiResponse<T>>
     */
    @PostMapping("/delete")
    @CheckMenuPermission(permission = PermissionType.DELETE)
    public ResponseEntity<ApiResponse<T>> deleteAdmin1101(
            @LogParam @Valid @RequestBody Admin1101DeleteDTO dto) {
        admin1101Service.deleteAdmin1101(dto);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
    }
    
    /**
     * 정기발간자료 엑셀 다운로드
     *
     * @param params EgovMap
     * @return ResponseEntity<byte[]>
     */
    @PostMapping("/excel/download")
    @CheckMenuPermission(permission = PermissionType.EXCEL)
    public ResponseEntity<byte[]> excelDownload(@LogParam @RequestBody EgovMap params) {
        EgovMapComponent egovMapComponent = new EgovMapComponent();
        EgovMap cond = egovMapComponent.convertToEgovMap(params);
        if(cond.get("reason") == null || cond.get("reason").toString().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }
        
        // FilterDTO로 변환
        Admin1101FilterDTO filter = new Admin1101FilterDTO();
        if (cond.get("searchKeyword") != null) {
            filter.setSearchKeyword(cond.get("searchKeyword").toString());
        }
        if (cond.get("openYn") != null) {
            filter.setOpenYn(cond.get("openYn").toString());
        }
        
        ExcelExportResult result = admin1101Service.excelDownload(filter);
        
        String fileName = result.getFileName();
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(result.getBytes());
    }

    /**
     * 파일 업로드
     * @param file 업로드할 파일
     * @param fileType 파일 구분 (COVER, FULL, SPECIAL, PAPER, APPENDIX)
     */
    @PostMapping("/upload")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<Long>> uploadFile(@RequestParam("file") MultipartFile file,
                                                         @RequestParam("fileType") String fileType,
                                                         @AuthenticationPrincipal BaseUser user) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, "파일이 존재하지 않습니다."));
        }

        Long fileOid = admin1101Service.saveFile(user.getUserOid(), file, fileType);

        return ResponseEntity.ok(ApiResponse.success(fileOid));
    }
}
