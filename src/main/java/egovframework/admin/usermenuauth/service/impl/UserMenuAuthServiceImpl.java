package egovframework.admin.usermenuauth.service.impl;

import egovframework.admin.usermenuauth.domain.MenuDto;
import egovframework.admin.usermenuauth.mapper.UserMenuAuthMapper;
import egovframework.admin.usermenuauth.service.UserMenuAuthService;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.board.domain.BoardRequestDto;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMenuAuthServiceImpl extends EgovAbstractServiceImpl implements UserMenuAuthService {
    private final UserMenuAuthMapper userMenuAuthMapper;

    @Override
    public List<MenuDto> selectMyMenuList(BaseUser user) {
        return userMenuAuthMapper.selectMyMenuList(user);
    }

    @Override
    public MenuDto selectBoardMenuDetail(BoardRequestDto boardRequestDto) {
        return userMenuAuthMapper.selectMenuByBoardInfo(boardRequestDto);
    }
}
