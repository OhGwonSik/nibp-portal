package egovframework.admin.usermenuauth.service;

import egovframework.admin.usermenuauth.domain.MenuDto;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.board.domain.BoardRequestDto;

import java.util.List;

public interface UserMenuAuthService {

    /**
     * 관리자 메뉴 목록 조회
     * @return List<MenuDto> 관리자 메뉴 목록
     */
    List<MenuDto> selectMyMenuList(BaseUser user);

    /**
     * 게시판 메뉴 상세 조회
     * @param boardRequestDto 게시판 요청 DTO
     * @return 메뉴 DTO
     */
    MenuDto selectBoardMenuDetail(BoardRequestDto boardRequestDto);
}
