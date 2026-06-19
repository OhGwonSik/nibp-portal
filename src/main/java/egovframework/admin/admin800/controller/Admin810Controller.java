package egovframework.admin.admin800.controller;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin810;
import egovframework.admin.admin800.domain.Admin810DTO;
import egovframework.admin.admin800.domain.Admin810FilterDTO;
import egovframework.admin.admin800.domain.Admin810VO;
import egovframework.admin.admin800.service.Admin810Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.api.ApiResponse;
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

/**
 * @ClassName : Admin810Controller.java
 * @Description : 기관 관리 controller
 *
 * @author : balee
 * @since : 2025. 11. 11
 * @version : 1.0
 *
 * << 개정이력(Modification Information) >>
 *
 *      수정일         수정자              수정내용
 *  -------------  ------------   ---------------------
 *   2025. 11. 11     balee             최초 생성
 *   2025. 11. 18     balee      CheckMenuPermission 적용
 *
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin810/org")
@PreAuthorize("hasRole('ADMIN')")
public class Admin810Controller {
    private final Admin810Service admin810Service;

    /**
     * 기관 목록 조회
     *
     * @param filter       Admin810FilterDTO
     * @return ResponseEntity<ApiResponse<PageInfo<Admin810VO>>>
     */
    @GetMapping("/list")
    @CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<PageInfo<Admin810VO>>> selectAdmin810List(@LogParam @Valid @ModelAttribute Admin810FilterDTO filter) {
        PageInfo<Admin810VO> response = admin810Service.selectAdmin810List(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 기관 단건 조회
     *
     * @param instOid       기관번호
     * @return ResponseEntity<ApiResponse<admin810List>>
     */
    @GetMapping("/select")
    @CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<Admin810VO>> selectAdmin810(@LogParam @RequestParam("instOid") String instOid) {
        Admin810VO admin810 = admin810Service.selectAdmin810(instOid);

        if (admin810 == null) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.NOT_FOUND, "기관 정보를 찾을 수 없습니다."), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(ApiResponse.success(admin810), HttpStatus.OK);
    }

    /**
     * 기관 등록
     *
     * @param admin810       Admin810
     * @return ResponseEntity<ApiResponse<Void>>
     */
    @PutMapping("/insert")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<T>> insertAdmin810(@LogParam @RequestBody @Valid Admin810 admin810,
                                                         @AuthenticationPrincipal BaseUser user) {
        // 로그인 O - 사용자 ID를 등록자,수정자 ID로 설정
        if (user != null) {
            admin810.setRegId(user.getUserId());
            admin810.setMdfcnId(user.getUserId());
        }
        admin810Service.insertAdmin810(admin810);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
    }

    /**
     * 기관 수정
     *
     * @param admin810       Admin810
     * @return ResponseEntity<ApiResponse<Void>>
     */
    @PatchMapping("/update")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<T>> updateAdmin810(@LogParam @RequestBody @Valid Admin810 admin810,
                                                         @AuthenticationPrincipal BaseUser user) {
        // 사용자 ID를 수정자 ID로 설정
        if (user != null) {
            admin810.setMdfcnId(user.getUserId());
        }
        admin810Service.updateAdmin810(admin810);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
    }

    /**
     * 기관 삭제
     *
     * @param admin810DTO    Admin810DTO
     * @return ResponseEntity<ApiResponse<Void>>
     */
    @PatchMapping("/delete")
    @CheckMenuPermission(permission = PermissionType.DELETE)
    public ResponseEntity<ApiResponse<T>> deleteAdmin810(@LogParam @RequestBody Admin810DTO admin810DTO,
                                                         @AuthenticationPrincipal BaseUser user) {
        admin810Service.deleteAdmin810(admin810DTO);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
    }
}
