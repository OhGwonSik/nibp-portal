package egovframework.admin.admin800.controller;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin807DTO;
import egovframework.admin.admin800.domain.Admin807DeleteDTO;
import egovframework.admin.admin800.domain.Admin807FilterDTO;
import egovframework.admin.admin800.domain.Admin807VO;
import egovframework.admin.admin800.service.Admin807Service;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @ClassName : Admin807Controller.java
 * @Description : 게시판 관리 controller (현재 게시글의 상태값은 생명윤리QNA만 관리중)
 *
 * @author : 
 * @since : 2025. 11. 14
 * @version : 1.0
 *
 * << 개정이력(Modification Information) >>
 *
 *      수정일         수정자              수정내용
 *  -------------  ------------   ---------------------
 *   2025. 11. 11     5             최초 생성
 *
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin807/board")
public class Admin807Controller {
    private final Admin807Service admin807Service;
    private final EgovMapComponent egovMapComponent;

    /**
     * 게시판 목록 조회 (페이징)
     *
     * @param filter       Admin807FilterDTO
     * @return ResponseEntity<ApiResponse<PageInfo<Admin807VO>>>
     */
    @GetMapping("/list")
    @CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<PageInfo<Admin807VO>>> selectBoardList(@LogParam @Valid Admin807FilterDTO filter) {
        PageInfo<Admin807VO> response = admin807Service.selectBoardList(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 게시판 저장 
     *
     * @param admin807DTO       Admin807DTO
     * @return ResponseEntity<ApiResponse<Integer>>
     */
    @PutMapping("/save")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<Integer>> insertBoard(@LogParam @RequestBody @Valid Admin807DTO admin807DTO,
                                                            @AuthenticationPrincipal BaseUser user) {
        // 사용자 ID를 등록자, 수정자 ID로 설정
        if (user != null) {
            admin807DTO.setRegId(user.getUserId());
            admin807DTO.setMdfcnId(user.getUserId());
        }
        int response = admin807Service.insertBoard(admin807DTO);
        return ResponseEntity.ok(ApiResponse.success(response));
    }   
    
    /**
     * 게시판 수정
     *
     * @param admin807DTO       Admin807DTO
     * @return ResponseEntity<ApiResponse<Integer>>
     */
    @PatchMapping("/update")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<Integer>> updateBoard(@LogParam @RequestBody @Valid Admin807DTO admin807DTO,
                                                            @AuthenticationPrincipal BaseUser user) {
        // 사용자 ID를 수정자 ID로 설정
        if (user != null) {
            admin807DTO.setMdfcnId(user.getUserId());
        }
        int response = admin807Service.updateBoard(admin807DTO);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 게시판 삭제 (soft delete)
     *
     * @param admin807DeleteDTO       admin807DeleteDTO
     * @return ResponseEntity<ApiResponse<Integer>>
     */
    @PatchMapping("/delete")
    @CheckMenuPermission(permission = PermissionType.DELETE)
    public ResponseEntity<ApiResponse<Integer>> deleteBoard(@LogParam @RequestBody @Valid Admin807DeleteDTO admin807DeleteDTO){
        int response = admin807Service.deleteBoard(admin807DeleteDTO);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 게시판 엑셀 다운로드
     *
     * @param params       EgovMap
     * @return ResponseEntity<byte[]>
     */
    @PostMapping("/excel/export")
    @CheckMenuPermission(permission = PermissionType.EXCEL)
    public ResponseEntity<byte[]> admin807ExportExcel(@LogParam @RequestBody EgovMap params) throws IOException {
        EgovMap cond = egovMapComponent.convertToEgovMap(params);
				
        if(cond.get("reason") == null || cond.get("reason").toString().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }
        ExcelExportResult result = admin807Service.admin807ExportExcel(cond);

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
