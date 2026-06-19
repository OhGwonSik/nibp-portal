package egovframework.common.auth.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import egovframework.common.audit.domain.ApiAccessLog;
import egovframework.common.audit.domain.PermissionChangeLog;
import egovframework.common.audit.enums.LoginResult;
import egovframework.common.audit.service.AuditService;
import egovframework.common.auth.domain.AdminUserUpdateDto;
import egovframework.common.auth.domain.AdminUserUpdatePwdDto;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.auth.domain.BaseUserDto;
import egovframework.common.auth.domain.CheckEmailRequestDto;
import egovframework.common.auth.domain.CheckIdRequestDto;
import egovframework.common.auth.domain.CheckPhoneRequestDto;
import egovframework.common.auth.domain.FindIdRequestDto;
import egovframework.common.auth.domain.FindPasswordRequestDto;
import egovframework.common.auth.domain.LoginResponseDto;
import egovframework.common.auth.domain.NicePassVerifyCallbackDto;
import egovframework.common.auth.domain.PasswordResetConfirmDto;
import egovframework.common.auth.domain.PasswordResetInitiateDto;
import egovframework.common.auth.mapper.AuthMapper;
import egovframework.common.auth.service.AuthService;
import egovframework.common.auth.service.RefreshTokenService;
import egovframework.common.auth.service.SessionService;
import egovframework.common.component.AESComponent;
import egovframework.common.constant.Constants;
import egovframework.common.jwt.JwtTokenProvider;
import egovframework.common.util.CookieUtil;
import egovframework.common.util.CryptoUtil;
import egovframework.common.util.RequestUtil;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends EgovAbstractServiceImpl implements AuthService {
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final CryptoUtil cryptoUtil;
    private final AESComponent aesComponent;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;
    private final CookieUtil cookieUtil;

    // 토큰만료시간
    private static final long VERIFICATION_TOKEN_EXPIRATION_MINUTES = 10;
    // private static final long PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES = 30;

    @Override
    public BaseUser authenticate(String userId, String password) {
        BaseUser authUser = authMapper.selectActiveUserByUserId(userId, aesComponent.getSecretKey());

        if (authUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자 정보를 찾을 수 없습니다.");
        }

        if (!authUser.isAccountNonLocked()) {
            throw new LockedException("로그인 실패 횟수를 초과하여 계정이 잠겼습니다. 비밀번호 초기화를 진행해주세요.");
        }

        if (!passwordEncoder.matches(password, authUser.getPswd())) {
            auditService.recordLoginFailure(userId);
            throw new BadCredentialsException("아이디나 비밀번호가 일치하지 않습니다.");
        }

        // 인증 성공 시, 관련 상태 업데이트
        auditService.processLoginSuccess(userId);

        return authUser;
    }

    @Override
    public BaseUser getUserById(String userId) {
        log.debug("Getting user by ID: {}", userId);
        return authMapper.selectUserByUserId(userId, aesComponent.getSecretKey());
    }

    @Override
    public BaseUser getActiveUserById(String userId) {
        log.debug("Getting active user by ID: {}", userId);
        return authMapper.selectActiveUserByUserId(userId, aesComponent.getSecretKey());
    }

    @Override
    public BaseUser getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);
        return authMapper.selectUserByEmail(email, aesComponent.getSecretKey());
    }

    @Override
    public BaseUser getUserByNo(Long userOid) {
        log.debug("Getting user by userOid: {}", userOid);
        return authMapper.selectUserByUserOid(userOid, aesComponent.getSecretKey());
    }

    @Override
    public String initiatePasswordReset(PasswordResetInitiateDto initiateDto) {
        log.debug("Initiating password reset for user: {}", initiateDto.getUserId());
        BaseUser user = authMapper.selectActiveUserByUserId(initiateDto.getUserId(), aesComponent.getSecretKey());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        String verificationToken = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(VERIFICATION_TOKEN_EXPIRATION_MINUTES);

        authMapper.insertVerificationToken(initiateDto.getUserId(), verificationToken, expiryDate);
        log.info("Verification token generated for user {}: {}", initiateDto.getUserId(), verificationToken);
        return verificationToken;
    }

    @Override
    public void validatePasswordResetToken(String passwordResetToken) {
        log.debug("Validating password reset token: {}", passwordResetToken);
        Map<String, Object> tokenInfo = authMapper.findPasswordResetToken(passwordResetToken);

        if (tokenInfo == null || ((LocalDateTime) tokenInfo.get("expiryDate")).isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("유효하지 않거나 만료된 비밀번호 재설정 토큰입니다.");
        }
        log.info("Password reset token {} is valid.", passwordResetToken);
    }

    @Override
    public Integer resetPassword(PasswordResetConfirmDto confirmDto) {
        Long userOid = confirmDto.getUserOid();
        String newPswd = confirmDto.getNewPswd();

        String encPswd = passwordEncoder.encode(newPswd);
        return authMapper.updateUserPassword(userOid, encPswd);
    }

    @Override
    public int insertJoinAdminUser(BaseUser adminUser) {
        log.debug("Joining admin user: {}", adminUser.getUserId());

        BaseUser checkUser = authMapper.selectAdminUserByUserId(adminUser.getUserId(), aesComponent.getSecretKey());
        if (checkUser != null) {
            throw new DuplicateKeyException("중복된 ID 입니다.");
        }

        adminUser.setPswd(passwordEncoder.encode(adminUser.getPswd()));
        adminUser.setLgnFailCnt(0);
        adminUser.setUseYn("Y");
        adminUser.setUserAuthrt("ADMIN");
        adminUser.setRegId(Constants.SYSTEM_ID);
        adminUser.setRegDt(LocalDateTime.now());
        adminUser.setMdfcnId(Constants.SYSTEM_ID);
        adminUser.setMdfcnDt(LocalDateTime.now());

        int result = authMapper.insertJoinAdminUser(cryptoUtil.encrypt(adminUser));

        if (result == 0) {
            throw new RuntimeException("관리자 등록에 실패했습니다.");
        }

        return result;
    }

    @Override
    public int checkAdminId(CheckIdRequestDto checkIdRequestDto) {
        return authMapper.checkAdminId(checkIdRequestDto);
    }

    @Override
    public boolean checkEmail(CheckEmailRequestDto checkEmailRequestDto) {
        return authMapper.checkEmail(checkEmailRequestDto, aesComponent.getSecretKey()) > 0;
    }

    @Override
    public boolean checkPhone(CheckPhoneRequestDto checkPhoneRequestDto) {
        return authMapper.checkPhone(checkPhoneRequestDto, aesComponent.getSecretKey()) > 0;
    }

    @Override
    public BaseUserDto updateAdminMe(AdminUserUpdateDto adminUserUpdateDto, BaseUser principal) {
        try {
            adminUserUpdateDto.setMdfcnDt(LocalDateTime.now());

            int result = authMapper.updateAdminMe(cryptoUtil.encrypt(adminUserUpdateDto), principal);

            if (result == 0) {
                throw new RuntimeException("관리자 정보 수정에 실패했습니다.");
            }

            BaseUser updatedUser = authMapper.selectAdminUserByUserId(principal.getUserId(), aesComponent.getSecretKey());
            if (updatedUser == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND, "관리자 정보 조회에 실패했습니다.");
            }

            BaseUserDto userDto = BaseUserDto.fromUser(updatedUser);

            return userDto;
        } catch (Exception e) {
            log.error("Update admin user error", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "관리자 정보 수정에 실패했습니다.");
        }
    }

    @Override
    public Integer updateAdminMePswd(AdminUserUpdatePwdDto adminUserUpdatePwdDto) {
        BaseUser principal = SecurityUtil.getUser();
        if (principal == null) {
            throw new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다.");
        }

        BaseUser authUser = authMapper.selectAdminUserByUserId(principal.getUserId(), aesComponent.getSecretKey());

        if (authUser != null && !passwordEncoder.matches(adminUserUpdatePwdDto.getCurrentPswd(), authUser.getPswd())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!adminUserUpdatePwdDto.getNewPswd().equals(adminUserUpdatePwdDto.getNewPwdConfirm())) {
            throw new RuntimeException("새 비밀번호가 일치하지 않습니다.");
        }

        adminUserUpdatePwdDto.setMdfcnDt(LocalDateTime.now());
        adminUserUpdatePwdDto.setNewPswd(passwordEncoder.encode(adminUserUpdatePwdDto.getNewPswd()));

        Integer result = authMapper.updateAdminMePswd(adminUserUpdatePwdDto, principal);

        if (result == 0) {
            throw new RuntimeException("관리자 비밀번호 수정에 실패했습니다.");
        }

        return result;
    }

    @Override
    public void logPermissionChange(List<PermissionChangeLog> permissionChangeLogs) {
        auditService.logPermissionChanges(permissionChangeLogs);
    }

    @Override
    public Long getUserByNiceCallbackDto(NicePassVerifyCallbackDto requestDto) {
        log.info("[NICE][SERVICE] dto = {}", requestDto);
        Long userOid = authMapper.getUserByNiceCallbackDto(requestDto , aesComponent.getSecretKey());
        log.info("[NICE][SERVICE] mapper returned userOid = {}", userOid);
        return authMapper.getUserByNiceCallbackDto(requestDto , aesComponent.getSecretKey());
    }

    @Override
    public String findAdminId(FindIdRequestDto requestDto) {
        log.debug("Finding admin ID for user: {}", requestDto.getUserName());
        return authMapper.findAdminIdByUserInfo(requestDto, aesComponent.getSecretKey());
    }

    @Override
    public String findAdminPasswordAndSendEmail(FindPasswordRequestDto requestDto) throws Exception {
        log.debug("Finding admin password for user: {}", requestDto.getUserId());
        BaseUser user = authMapper.findAdminUserByPasswordInfo(requestDto, aesComponent.getSecretKey());

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "입력하신 정보와 일치하는 관리자 계정을 찾을 수 없습니다.");
        }

        // 임시 비밀번호 생성 (8자리: 대문자+소문자+숫자+특수문자)
        String tempPassword = generateTempPassword();

        // 임시 비밀번호로 업데이트
        String encodedPassword = passwordEncoder.encode(tempPassword);
        int updateResult = authMapper.updatePasswordByUserOid(user.getUserOid(), encodedPassword);

        if (updateResult == 0) {
            throw new RuntimeException("임시 비밀번호 설정에 실패했습니다.");
        }

        log.info("[TEMP_PWD] Temporary password updated for admin userId: {}", user.getUserId());

        return tempPassword;
    }

    private String generateTempPassword() {
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String special = "!@#$%^&*";

        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();

        // 각 카테고리에서 최소 1개씩 선택
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // 나머지 4자리는 랜덤
        String allChars = uppercase + lowercase + numbers + special;
        for (int i = 0; i < 4; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 셔플
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    @Override
    public LoginResponseDto issueTokensAndCreateSession(
            BaseUser authenticatedUser,
            HttpServletRequest request,
            HttpServletResponse response,
            String loginSessionId) {

        String userId = authenticatedUser.getUserId();
        String userAuthrt = authenticatedUser.getUserAuthrt();

        // 1. loginSessionId 결정: 전달받은 값 > 익명 쿠키 계승 > 새로 생성
        String loginSsnId = loginSessionId;
        if (loginSsnId == null) {
            loginSsnId = CookieUtil.getAnonymousLoginSessionId(request);
        }
        if (loginSsnId == null) {
            loginSsnId = jwtTokenProvider.generateLoginSessionId();
            log.info("[LoginSsnId] Generated NEW loginSessionId: {} (userId: {})", loginSsnId, userId);
        } else {
            log.info("[LoginSsnId] Using loginSessionId: {} (userId: {})", loginSsnId, userId);
        }

        // 2. ApiAccessLog 업데이트
        try {
            ApiAccessLog apiAccessLog = (ApiAccessLog) request.getAttribute("apiAccessLog");
            if (apiAccessLog != null) {
                String oldLoginSsnId = apiAccessLog.getSsnId();
                apiAccessLog.setSsnId(loginSsnId);
                log.info("[LoginSsnId] Updated ApiAccessLog: {} -> {}", oldLoginSsnId, loginSsnId);
            }
        } catch (Exception e) {
            log.error("[LoginSsnId] Failed to update ApiAccessLog: {}", e.getMessage());
        }

        // 3. 로그인 성공 감사 로그
        auditService.logLoginAttempt(request, userId, authenticatedUser, LoginResult.SUCCESS, null, loginSsnId);

        // 4. 토큰 생성
        long accessTokenExp = jwtTokenProvider.getAccessTokenExpiration(userAuthrt);
        long refreshTokenExp = jwtTokenProvider.getRefreshTokenExpiration(userAuthrt);

        String accessToken = jwtTokenProvider.generateAccessToken(authenticatedUser, loginSsnId);
        String refreshToken = refreshTokenService.createRefreshToken(userId, userAuthrt, loginSsnId);
        log.info("[LoginSsnId] Generated tokens with loginSessionId: {}", loginSsnId);

        // 5. Redis 세션 생성
        String refreshTokenId = jwtTokenProvider.getTokenId(refreshToken);
        sessionService.createSession(
                authenticatedUser,
                accessToken,
                refreshTokenId,
                loginSsnId,
                RequestUtil.getRemoteIpAddress(),
                request.getHeader("User-Agent")
        );

        // 6. 쿠키 세팅
        response.addHeader("Set-Cookie", cookieUtil.createAccessTokenCookie(accessToken, (int) (accessTokenExp / 1000)));
        response.addHeader("Set-Cookie", cookieUtil.createRefreshTokenCookie(refreshToken, (int) (refreshTokenExp / 1000)));
        response.addHeader("Set-Cookie", cookieUtil.createAnonymousLoginSessionIdCookie(loginSsnId, 60 * 60 * 24 * 30));

        // 7. LoginResponseDto 빌드
        BaseUserDto userDto = BaseUserDto.fromUser(authenticatedUser);

        return LoginResponseDto.builder()
                .user(userDto)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessTokenExp / 1000)
                .build();
    }
}
