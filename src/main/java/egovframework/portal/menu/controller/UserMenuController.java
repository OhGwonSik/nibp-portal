package egovframework.portal.menu.controller;

import egovframework.common.api.ApiResponse;
import egovframework.portal.menu.domain.UserMenuDTO;
import egovframework.portal.menu.service.UserMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName : UserMenuController.java
 * @Description : 사용자 메뉴 관리 controller
 *
 * @author : balee
 * @since : 2025. 12. 17
 * @version : 1.0
 *
 * << 개정이력(Modification Information) >>
 *
 *      수정일         수정자              수정내용
 *  -------------  ------------   ---------------------
 *   2025. 12. 17     balee             최초 생성
 *
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/menu")
public class UserMenuController {

    private final UserMenuService menuService;

    /**
     * 사용자 메뉴 목록 조회
     *
     * @return ResponseEntity<ApiResponse<List<UserMenuDTO>>>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserMenuDTO>>> selectMenuList() {
        List<UserMenuDTO> menus = menuService.selectMenuList();
        return ResponseEntity.ok(ApiResponse.success(menus));
    }
}
