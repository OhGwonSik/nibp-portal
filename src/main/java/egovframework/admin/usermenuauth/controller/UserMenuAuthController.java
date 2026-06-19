package egovframework.admin.usermenuauth.controller;

import egovframework.admin.usermenuauth.domain.MenuDto;
import egovframework.admin.usermenuauth.service.UserMenuAuthService;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menu")
@RequiredArgsConstructor
public class UserMenuAuthController {
    private final UserMenuAuthService userMenuAuthService;

    @GetMapping("/my_menu")
    public ResponseEntity<ApiResponse<List<MenuDto>>> selectMyMenuList(@AuthenticationPrincipal BaseUser user) {
        if(!"ADMIN".equals(user.getUserAuthrt())) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.BAD_REQUEST, "권한이 없습니다."), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(ApiResponse.success(userMenuAuthService.selectMyMenuList(user)), HttpStatus.OK);
    }
    
}
