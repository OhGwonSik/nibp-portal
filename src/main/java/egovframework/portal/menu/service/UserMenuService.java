package egovframework.portal.menu.service;

import egovframework.common.board.domain.BoardRequestDto;
import egovframework.portal.menu.domain.UserMenuDTO;

import java.util.List;

/**
 * @ClassName : UserMenuService.java
 * @Description : 사용자 메뉴 관리 서비스 인터페이스
 *
 * @author : balee
 * @since : 2025. 12. 17
 * @version : 1.0
 */
public interface UserMenuService {

    /**
     * 사용자 메뉴 목록 조회
     * @return List<UserMenuDTO> 사용자 메뉴 목록
     */
    List<UserMenuDTO> selectMenuList();


    UserMenuDTO selectMenuByMenuCd(String menuCd);

    /**
     * 게시판 메뉴 상세 조회
     * @param boardRequestDto 게시판 요청 DTO
     * @return 메뉴 DTO
     */
    UserMenuDTO selectBoardMenuDetail(BoardRequestDto boardRequestDto);
    
    /**
     * 전체 1레벨 메뉴 목록 COMMON, HIDDEN 제외 (통합검색 분류 카테고리용)
     * @param
     * @return List<UserMenuDTO> 전체 1레벨 메뉴 목록
     */
    List<UserMenuDTO> selectRootMenuList();

    /**
     * 동적 게시판 메뉴-게시판 연결 검증
     * @param menuCd 메뉴 코드
     * @param bbsOid 게시판 OID
     * @return 유효한 경우 메뉴 DTO, 아닌 경우 null
     */
    UserMenuDTO selectValidBoardMenu(String menuCd, Long bbsOid);

    /**
     * 메뉴 캐시 무효화 (관리자 페이지에서 메뉴 변경 시 호출)
     */
    void evictMenuCache();
}