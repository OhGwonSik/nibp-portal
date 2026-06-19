package egovframework.admin.usermenuauth.mapper;

import egovframework.admin.usermenuauth.domain.MenuDto;
import egovframework.admin.usermenuauth.domain.UserMenuAuthDto;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.board.domain.BoardRequestDto;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserMenuAuthMapper {
    List<MenuDto> selectMyMenuList(BaseUser user);
    UserMenuAuthDto selectValidAuth(@Param("userOid") Long userOid, @Param("menuOid") Long menuNo, @Param("now") LocalDate now);
    MenuDto selectMenuByBoardInfo(BoardRequestDto boardRequestDto);
}
