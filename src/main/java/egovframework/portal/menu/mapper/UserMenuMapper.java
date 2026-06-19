package egovframework.portal.menu.mapper;

import egovframework.common.board.domain.BoardRequestDto;
import egovframework.portal.menu.domain.UserMenuDTO;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

/**
 * @ClassName : UserMenuMapper.java
 * @Description : 사용자 메뉴 관련 Mapper
 *
 * @author : balee
 * @since  : 2025. 12. 17
 * @version : 1.0
 */
@Mapper
public interface UserMenuMapper {
    List<UserMenuDTO> selectMenuList(String menuAuthLv);

    UserMenuDTO selectMenuByMenuCd(String menuCd);

    UserMenuDTO selectMenuByBoardInfo(BoardRequestDto boardRequestDto);
    
    List<UserMenuDTO> selectRootMenuList();

    UserMenuDTO selectValidBoardMenu(@Param("menuCd") String menuCd, @Param("bbsOid") Long bbsOid);
}
