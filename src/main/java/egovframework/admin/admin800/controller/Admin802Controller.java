package egovframework.admin.admin800.controller;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.*;
import egovframework.admin.admin800.service.Admin802Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.enums.PermissionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @ClassName : Admin802Controller.java
 * @Description : 공통코드 관리 controller
 *
 * @author : balee
 * @since : 2025. 11. 18
 * @version : 1.0
 *
 * << 개정이력(Modification Information) >>
 *
 *      수정일         수정자              수정내용
 *  -------------  ------------   ---------------------
 *   2025. 11. 18     balee             최초 생성
 *
 */
@Slf4j // log
@RestController // json api
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin802")
@PreAuthorize("hasRole('ADMIN')")
public class Admin802Controller {
	private final Admin802Service admin802Service;

	/**
	 * 그룹코드 목록 조회
	 *
	 * @param filter       GroupCodeFilterDTO
	 * @return ResponseEntity<ApiResponse<PageInfo<GroupCodeResponseDTO>>>
	 */
	@GetMapping("/groupcode/filter")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<PageInfo<GroupCodeResponseDTO>>> selectGroupcodeWithFilter(@LogParam @Valid @ModelAttribute GroupCodeFilterDTO filter) {
		PageInfo<GroupCodeResponseDTO> response = admin802Service.selectGroupcodeWithFilter(filter);
		return ResponseEntity.ok(ApiResponse.success(response));
    }

	/**
	 * 코드 목록 조회
	 *
	 * @param filter       CodeFilterDTO
	 * @return ResponseEntity<ApiResponse<PageInfo<CodeResponseDTO>>>
	 */
	@GetMapping("/code/filter")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<PageInfo<CodeResponseDTO>>> selectCodeWithFilter(@LogParam @Valid @ModelAttribute CodeFilterDTO filter) {
		PageInfo<CodeResponseDTO> response = admin802Service.selectCodeWithFilter(filter);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 그룹코드 등록
	 *
	 * @param groupCode    GroupCode
	 * @return ResponseEntity<ApiResponse<Integer>>
	 */
	@PutMapping("/groupcode/insert")
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<Integer>> insertAdmin802Group(@LogParam @RequestBody @Valid GroupCode groupCode,
																	@AuthenticationPrincipal BaseUser user) {
		// 사용자 ID를 등록자,수정자 ID로 설정
		if (user != null) {
			groupCode.setRegId(user.getUserId());
			groupCode.setMdfcnId(user.getUserId());
		}
		Integer result = admin802Service.insertAdmin802Group(groupCode);
		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
	}

	/**
	 * 코드 등록
	 *
	 * @param code         Code
	 * @return ResponseEntity<ApiResponse<Integer>>
	 */
	@PutMapping("/code/insert")
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<Integer>> insertAdmin802Code(@LogParam @RequestBody @Valid Code code,
																   @AuthenticationPrincipal BaseUser user) {
		// 사용자 ID를 등록자,수정자 ID로 설정
		if (user != null) {
			code.setRegId(user.getUserId());
			code.setMdfcnId(user.getUserId());
		}
		Integer result = admin802Service.insertAdmin802Code(code);
		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
	}

	/**
	 * 그룹코드 수정
	 *
	 * @param groupCode    GroupCode
	 * @return ResponseEntity<ApiResponse<Integer>>
	 */
	@PatchMapping("/groupcode/update")
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<Integer>> updateAdmin802Group(@LogParam @RequestBody @Valid GroupCode groupCode,
																	@AuthenticationPrincipal BaseUser user) {
		// 사용자 ID를 등록자,수정자 ID로 설정
		if (user != null) {
			groupCode.setRegId(user.getUserId());
			groupCode.setMdfcnId(user.getUserId());
		}
		Integer result = admin802Service.updateAdmin802Group(groupCode);
		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
	}

	/**
	 * 코드 수정
	 *
	 * @param code         Code
	 * @return ResponseEntity<ApiResponse<Integer>>
	 */
	@PatchMapping("/code/update")
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<Integer>> updateAdmin802Code(@LogParam @RequestBody @Valid Code code,
																   @AuthenticationPrincipal BaseUser user) {
		// 사용자 ID를 등록자,수정자 ID로 설정
		if (user != null) {
			code.setRegId(user.getUserId());
			code.setMdfcnId(user.getUserId());
		}
		Integer result = admin802Service.updateAdmin802Code(code);
		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
	}


	@PostMapping("/code/delete")
	@CheckMenuPermission(permission = PermissionType.DELETE)
	public ResponseEntity<ApiResponse<Integer>> deleteAdmin802(@LogParam @RequestBody Code code) {
		Integer result = 0;
		// Integer result = admin802Service.deleteAdmin802(code);
		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
	}
	
	@PostMapping("/groupcode/delete")
	@CheckMenuPermission(permission = PermissionType.DELETE)
	public ResponseEntity<ApiResponse<Integer>> deleteAdmin802Group(@LogParam @RequestBody GroupCode groupCode) {
		Integer result = 0;
		// Integer result = admin802Service.deleteAdmin802Group(groupCode);
		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
	}
}
