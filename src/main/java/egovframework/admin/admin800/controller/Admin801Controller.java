package egovframework.admin.admin800.controller;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.*;
import egovframework.admin.admin800.service.Admin801Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.enums.PermissionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @ClassName : Admin801Controller.java
 * @Description : 관리자 관리 controller
 *
 * @author : balee
 * @since : 2025. 11. 19
 * @version : 1.0
 *
 * << 개정이력(Modification Information) >>
 *
 *      수정일         수정자              수정내용
 *  -------------  ------------   ---------------------
 *   2025. 11. 19     balee             최초 생성
 *
 */

@Slf4j // log
@RestController // json api
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin801")
@PreAuthorize("hasRole('ADMIN')")
public class Admin801Controller {
	private final Admin801Service admin801Service;

    /**
     * 관리자 목록 조회
     *
     * @param filter       Admin801filterDto
     * @return ResponseEntity<ApiResponse<PageInfo<Admin801ResponseDto>>>
     */
    @PostMapping("/users/filter")
    @CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<PageInfo<Admin801ResponseDto>>> selectAdmin801ListWithFilter(@LogParam @RequestBody Admin801filterDto filter) {
        PageInfo<Admin801ResponseDto> response = admin801Service.selectAdmin801ListWithFilter(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 관리자 상세 조회
     *
     * @param userOid       사용자번호
     * @return ResponseEntity<ApiResponse<Admin801VO>>
     */
    @GetMapping("/users/detail")
    @CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<Admin801VO>> selectAdmin801Detail(@LogParam @RequestParam Long userOid) {
        EgovMap map = new EgovMap();
        map.put("userOid", userOid);

        Admin801VO result = admin801Service.selectAdmin801Detail(map);
        if (result == null) {
                return new ResponseEntity<>(ApiResponse.error(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."), HttpStatus.NOT_FOUND);
            }
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }

    /**
     * 관리자 정보 저장
     *
     * @param admin801DTO       Admin801DTO
     */
    @PostMapping("/users/upsert")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<?>> upsertAdmin801(@LogParam @Valid @RequestBody Admin801DTO admin801DTO,
                                                         @AuthenticationPrincipal BaseUser user) {
        // 사용자 ID를 등록자, 수정자 ID로 설정
        if (user != null) {
            admin801DTO.setRegId(user.getUserId());
            admin801DTO.setMdfcnId(user.getUserId());
        }
        admin801Service.upsertAdmin801(admin801DTO);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
    }

    /**
     * 관리자 계정 잠금 해제
     *
     * @param unlockAccountDto 계정 잠금 해제 정보
     */
    @PostMapping("/users/unlock")
    public ResponseEntity<ApiResponse<Integer>> unlockAccount(@LogParam @Valid @RequestBody UnlockAccountDto unlockAccountDto,
                                                              @AuthenticationPrincipal BaseUser user) {
        // 사용자 ID를 수정자 ID로 설정
        if (user != null) {
            unlockAccountDto.setMdfcnId(user.getUserId());
        }
        Integer response = admin801Service.unlockAccount(unlockAccountDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
