package egovframework.admin.menu.mapper;

import egovframework.admin.menu.domain.MenuDto;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface MenuMapper {
//    Long selectMenuNoByMenuCd(String menuCd);
    List<MenuDto> selectMenusByMenuPage(String menuPage);
}

