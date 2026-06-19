package egovframework.admin.admin800.controller;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin803AccountVO;
import egovframework.admin.admin800.domain.Admin803AuthRequestDTO;
import egovframework.admin.admin800.domain.Admin803CopyAuthRequestDTO;
import egovframework.admin.admin800.domain.Admin803FilterDTO;
import egovframework.admin.admin800.domain.Admin803MenuDTO;
import egovframework.admin.admin800.service.Admin803Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.enums.PermissionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @ClassName : Admin803Controller.java
 * @Description : 권한 관리 controller
 *
 * @author : balee
 * @since : 2025. 11. 17
 * @version : 1.0
 *
 * << 개정이력(Modification Information) >>
 *
 *      수정일         수정자              수정내용
 *  -------------  ------------   ---------------------
 *   2025. 11. 17     balee             최초 생성
 *   2025. 11. 18     balee      CheckMenuPermission 적용
 *
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin803")
@PreAuthorize("hasRole('ADMIN')")
public class Admin803Controller {
    private final Admin803Service admin803Service;

    /**
     * 관리자 계정 목록 조회
     *
     * @param filter Admin803FilterDTO
     * @return ResponseEntity<ApiResponse<PageInfo<Admin803AccountVO>>>
     */
    @PostMapping("/account/list")
    @CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<PageInfo<Admin803AccountVO>>> selectAdmin803AccountList(@LogParam @Valid @RequestBody Admin803FilterDTO filter) {
        PageInfo<Admin803AccountVO> response = admin803Service.selectAdmin803AccountList(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 사용자별 메뉴 권한 목록 조회
     *
     * @param admin803MenuDTO Admin803MenuDTO
     * @return ResponseEntity<ApiResponse<List<Admin803MenuDTO>>>
     */
    @PostMapping("/menu/list")
    @CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<List<Admin803MenuDTO>>> selectAdmin803MenuList(@LogParam @RequestBody Admin803MenuDTO admin803MenuDTO) {
        List<Admin803MenuDTO> admin803List = admin803Service.selectAdmin803MenuList(admin803MenuDTO);
        return new ResponseEntity<>(ApiResponse.success(admin803List), HttpStatus.OK);
    }

    /**
     * 사용자 메뉴 권한 등록/수정
     *
     * @param admin803AuthRequestDTO Admin803AuthRequestDTO (changeReason 포함)
     * @return ResponseEntity<ApiResponse<T>>
     */
    @PatchMapping("/menu/update")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<T>> upsertAdmin803MenuAuth(@LogParam @RequestBody @Valid Admin803AuthRequestDTO admin803AuthRequestDTO,
                                                                 @AuthenticationPrincipal BaseUser user) {
        // 사용자 ID를 등록자 ID, 수정자 ID로 설정
        if (user != null) {
            admin803AuthRequestDTO.getAuthList().forEach(auth -> {
                auth.setRegId(user.getUserId());
                auth.setMdfcnId(user.getUserId());
            });
        }
        admin803Service.upsertAdmin803MenuAuth(admin803AuthRequestDTO.getAuthList(), admin803AuthRequestDTO.getChangeReason());
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
    }

    /**
     * 권한 복사 (소스 유저 → 타겟 유저 다수)
     *
     * @param requestDTO Admin803CopyAuthRequestDTO (sourceUserNo, targetUserNos, changeReason)
     * @param user BaseUser
     * @return ResponseEntity<ApiResponse<T>>
     */
    @PostMapping("/menu/copy")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<T>> copyMenuAuth(@LogParam @RequestBody @Valid Admin803CopyAuthRequestDTO requestDTO,
                                                       @AuthenticationPrincipal BaseUser user) {
        admin803Service.copyMenuAuth(requestDTO, user.getUserId());
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
    }

    /**
     * 개인정보취급권한 수정
     * 미사용처리
     * @param admin803AccountDTO Admin803AccountDTO
     * @param user BaseUser
     * @return ResponseEntity<ApiResponse<T>>
     */
    // @PatchMapping("/privacy/update")
    // @CheckMenuPermission(permission = PermissionType.WRITE)
    // public ResponseEntity<ApiResponse<T>> updateAdmin803PrivacyAuth(@RequestBody @Valid Admin803AccountDTO admin803AccountDTO,
    // 																 @AuthenticationPrincipal BaseUser user) {
    // 	// 사용자 ID를 수정자 ID로 설정
    // 	if (user != null) {
    // 		admin803AccountDTO.setUpdUserId(user.getUserId());
    // 	}
    // 	admin803Service.updateAdmin803PrivacyAuth(admin803AccountDTO);
    // 	return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);

    // }
}