package egovframework.admin.mypage.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.admin.mypage.domain.MypageUpdateDto;
import egovframework.admin.mypage.domain.MypageUpdatePwdDto;
import egovframework.admin.mypage.service.MypageService;
import egovframework.common.api.ApiResponse;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.auth.domain.BaseUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/mypage")
@PreAuthorize("hasRole('ADMIN')")
public class MypageController {
    private final MypageService mypageService;

    @GetMapping("/select")
    public ResponseEntity<ApiResponse<BaseUserDto>> getCurrentUser(@AuthenticationPrincipal BaseUser principal) {
        BaseUserDto userDto = mypageService.selectCurrentUser(principal);
        return new ResponseEntity<>(ApiResponse.success(userDto), HttpStatus.OK);
    }
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<BaseUserDto>> updateAdminMe(@Valid @RequestBody MypageUpdateDto mypageUpdateDto,
                                                                  @AuthenticationPrincipal BaseUser principal) {
        if (principal == null) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."), HttpStatus.UNAUTHORIZED);
        }
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.FORBIDDEN, "관리자만 접근 가능합니다."), HttpStatus.FORBIDDEN);
        }
        try {
            BaseUserDto userDto = mypageService.updateAdminMe(mypageUpdateDto, principal);
            return new ResponseEntity<>(ApiResponse.success(userDto), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Update current admin user error", e);
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "정보 수정 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/updatePswd")
    public ResponseEntity<ApiResponse<Integer>> updateAdminMePwd(@Valid @RequestBody MypageUpdatePwdDto mypageUpdatePwdDto,
                                                                 @AuthenticationPrincipal BaseUser principal) {
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "관리자만 접근 가능합니다."), HttpStatus.UNAUTHORIZED);
        }

        try {
            Integer result = mypageService.updateAdminMePwd(mypageUpdatePwdDto);
            return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Update current user password error", e);
            // return new ResponseEntity<>(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            throw e;
        }
    }
}
