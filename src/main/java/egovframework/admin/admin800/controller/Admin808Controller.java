package egovframework.admin.admin800.controller;

import egovframework.admin.admin800.domain.Admin808DTO;
import egovframework.admin.admin800.domain.Admin808DeleteDTO;
import egovframework.admin.admin800.service.Admin808Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.component.EgovMapComponent;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @ClassName : Admin808Controller.java
 * @Description : 메뉴 관리 controller
 *
 * @author : balee
 * @since : 2025. 11. 13
 * @version : 1.0
 *
 * << 개정이력(Modification Information) >>
 *
 *      수정일         수정자              수정내용
 *  -------------  ------------   ---------------------
 *   2025. 11. 13     balee             최초 생성
 *   2025. 11. 18     balee      CheckMenuPermission 적용
 *
 */
@Slf4j // log
@RestController // json api
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin808/menu")
@PreAuthorize("hasRole('ADMIN')")
public class Admin808Controller {
    private final Admin808Service admin808Service;
    private final EgovMapComponent egovMapComponent;

    /**
     * 메뉴 목록 조회
     *
     * @return ResponseEntity<ApiResponse<List<Admin808DTO>>>
     */
    @GetMapping("/list")
    @CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<List<Admin808DTO>>> selectAdmin808List() {
        List<Admin808DTO> admin808List = admin808Service.selectAdmin808List();
        return new ResponseEntity<>(ApiResponse.success(admin808List), HttpStatus.OK);
    }

    /**
     * 메뉴 등록
     *
     * @param admin808DTO    Admin808DTO
     * @return ResponseEntity<ApiResponse<Integer>>
     */
    @PutMapping("/insert")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<Integer>> insertAdmin808(@LogParam @RequestBody @Valid Admin808DTO admin808DTO,
                                                               @AuthenticationPrincipal BaseUser user) {
        // 사용자 ID를 등록자,수정자 ID로 설정
        if (user != null) {
            admin808DTO.setRegId(user.getUserId());
            admin808DTO.setMdfcnId(user.getUserId());
        }
        int result = admin808Service.insertAdmin808(admin808DTO);
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }

    /**
     * 메뉴 수정
     *
     * @param admin808DTO    Admin808DTO
     * @return ResponseEntity<ApiResponse<Integer>>
     */
    @PatchMapping("/update")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<Integer>> updateAdmin808(@LogParam @RequestBody @Valid Admin808DTO admin808DTO,
                                                               @AuthenticationPrincipal BaseUser user) {
        // 사용자 ID를 수정자 ID로 설정
        if (user != null) {
            admin808DTO.setMdfcnId(user.getUserId());
        }
        int result = admin808Service.updateAdmin808(admin808DTO);
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }

    /**
     * 메뉴 삭제
     *
     * @param admin808DeleteDTOS    List<Admin808DeleteDTO>
     * @return ResponseEntity<ApiResponse<Integer>>
     */
    @PatchMapping("/delete")
    @CheckMenuPermission(permission = PermissionType.DELETE)
    public ResponseEntity<ApiResponse<Integer>> deleteAdmin808(@LogParam @RequestBody @Valid List<Admin808DeleteDTO> admin808DeleteDTOS,
                                                               @AuthenticationPrincipal BaseUser user) {
        // 사용자 ID를 수정자 ID로 설정
        if (user != null) {
            for (Admin808DeleteDTO admin808DeleteDTO : admin808DeleteDTOS) {
                admin808DeleteDTO.setMdfcnId(user.getUserId());
            }
        }
        int result = admin808Service.deleteAdmin808(admin808DeleteDTOS);
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }

    /**
     * 메뉴 엑셀 다운로드
     *
     * @param params    EgovMap
     * @return ResponseEntity<byte[]>
     */
    @PostMapping("/excel/export")
    @CheckMenuPermission(permission = PermissionType.EXCEL)
    public ResponseEntity<byte[]> admin401ExportExcel(@LogParam @RequestBody EgovMap params) throws IOException {
        EgovMap cond = egovMapComponent.convertToEgovMap(params);
        if(cond.get("reason") == null || cond.get("reason").toString().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }
        ExcelExportResult result = admin808Service.admin808ExportExcel(cond);

        String fileName = result.getFileName();
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(result.getBytes());

    }
    
    @GetMapping("/next-seq")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<Integer>> getNextMenuSequence() {
		int result = admin808Service.getNextMenuSequence();
		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
	}
}