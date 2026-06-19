package egovframework.admin.mypage.service;

import egovframework.admin.mypage.domain.MypageUpdateDto;
import egovframework.admin.mypage.domain.MypageUpdatePwdDto;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.auth.domain.BaseUserDto;

public interface MypageService {
    BaseUserDto selectCurrentUser(BaseUser principal);
    BaseUserDto updateAdminMe(MypageUpdateDto mypageUpdateDto, BaseUser principal);
    Integer updateAdminMePwd(MypageUpdatePwdDto mypageUpdatePwdDto);
}