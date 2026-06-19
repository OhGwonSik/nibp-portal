package egovframework.admin.mypage.service.impl;

import java.time.LocalDateTime;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import egovframework.admin.mypage.domain.MypageUpdateDto;
import egovframework.admin.mypage.domain.MypageUpdatePwdDto;
import egovframework.admin.mypage.mapper.MypageMapper;
import egovframework.admin.mypage.service.MypageService;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.auth.domain.BaseUserDto;
import egovframework.common.component.AESComponent;
import egovframework.common.util.CryptoUtil;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MypageServiceImpl extends EgovAbstractServiceImpl implements MypageService {
    private final MypageMapper mypageMapper;
    private final PasswordEncoder passwordEncoder;
    private final CryptoUtil cryptoUtil;
    private final AESComponent aesComponent;

    @Override
    public BaseUserDto updateAdminMe(MypageUpdateDto mypageUpdateDto, BaseUser principal) {
        mypageUpdateDto.setUpdDt(LocalDateTime.now());
        cryptoUtil.encrypt(mypageUpdateDto);

        int result = mypageMapper.updateAdminMe(mypageUpdateDto, principal);

        if (result == 0) {
            throw new RuntimeException("관리자 정보 수정에 실패했습니다.");
        }

        BaseUser updatedUser = mypageMapper.selectAdminUserByUserId(principal.getUserId(), aesComponent.getSecretKey());
        if (updatedUser == null) {
            throw new RuntimeException("관리자 정보 조회에 실패했습니다.");
        }

        BaseUserDto userDto = BaseUserDto.fromUser(updatedUser);

        return userDto;
    }

    @Override
    public Integer updateAdminMePwd(MypageUpdatePwdDto mypageUpdatePwdDto) {
        BaseUser principal = SecurityUtil.getUser();
        if (principal == null) {
            throw new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다.");
        }

        BaseUser authUser = mypageMapper.selectAdminUserByUserId(principal.getUserId(), aesComponent.getSecretKey());

        if (authUser != null && !passwordEncoder.matches(mypageUpdatePwdDto.getCurrentPwd(), authUser.getPswd())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (authUser != null && passwordEncoder.matches(mypageUpdatePwdDto.getNewPswd(), authUser.getPswd())) {
            throw new RuntimeException("현재 비밀번호와 같습니다.");
        }

        if (!mypageUpdatePwdDto.getNewPswd().equals(mypageUpdatePwdDto.getNewPwdConfirm())) {
            throw new RuntimeException("새 비밀번호가 일치하지 않습니다.");
        }

        mypageUpdatePwdDto.setUpdDt(LocalDateTime.now());
        mypageUpdatePwdDto.setNewPswd(passwordEncoder.encode(mypageUpdatePwdDto.getNewPswd()));

        Integer result = mypageMapper.updateAdminMePwd(mypageUpdatePwdDto, principal);

        if (result == 0) {
            throw new RuntimeException("관리자 비밀번호 수정에 실패했습니다.");
        }

        return result;
    }

    @Override
    public BaseUserDto selectCurrentUser(BaseUser principal) {
        BaseUser user = mypageMapper.selectAdminUserByUserId(principal.getUserId(), aesComponent.getSecretKey());
        return BaseUserDto.fromUser(user);
    }
}
