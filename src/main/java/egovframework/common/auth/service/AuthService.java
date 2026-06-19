package egovframework.common.auth.service;

import egovframework.common.audit.domain.PermissionChangeLog;
import egovframework.common.auth.domain.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface AuthService {
    BaseUser authenticate(String userId, String password);

    BaseUser getUserById(String userId);

    BaseUser getActiveUserById(String userId);

    BaseUser getUserByEmail(String email);

    /**
     * 사용자 번호로 사용자 정보 조회
     * @param userOid 사용자 번호
     * @return BaseUser 사용자 정보
     */
    BaseUser getUserByNo(Long userOid);

    /**
     * 비밀번호 재설정 시작 (NICE PASS 인증을 위한 토큰 발급)
     * @param initiateDto 사용자 ID
     * @return NICE PASS 인증을 위한 verificationToken
     */
    String initiatePasswordReset(PasswordResetInitiateDto initiateDto);

    // /**
    //  * NICE PASS 인증 콜백 처리 및 비밀번호 재설정 토큰 발급
    //  * @param callbackDto NICE PASS 인증 결과
    //  * @return 비밀번호 재설정 토큰
    //  */
    // String completeNicePassVerification(NicePassVerifyCallbackDto callbackDto);

    /**
     * 비밀번호 재설정 토큰 유효성 검사
     * @param passwordResetToken 비밀번호 재설정 토큰
     */
    void validatePasswordResetToken(String passwordResetToken);

    /**
     * 비밀번호 재설정
     * @param confirmDto 비밀번호 재설정
     */
    Integer resetPassword(PasswordResetConfirmDto confirmDto);

    int insertJoinAdminUser(BaseUser adminUser);

    int checkAdminId(CheckIdRequestDto checkIdRequestDto);

    boolean checkEmail(CheckEmailRequestDto checkEmailRequestDto);

    boolean checkPhone(CheckPhoneRequestDto checkPhoneRequestDto);

    BaseUserDto updateAdminMe(AdminUserUpdateDto adminUserUpdateDto, BaseUser principal);

    Integer updateAdminMePswd(AdminUserUpdatePwdDto adminUserUpdatePwdDto);

    /**
     * 권한 변경 로그 기록
     */
    void logPermissionChange(List<PermissionChangeLog> permissionChangeLogs);

    /**
     * userId, 이름, 이메일, 휴대폰 번호로 사용자 조회
     * @param requestDto userId, 이름, 생년월일, 휴대폰 번호
     * @return 조회된 사용자 userOid
     */
    Long getUserByNiceCallbackDto(NicePassVerifyCallbackDto requestDto);

    /**
     * 관리자 아이디 찾기
     * @param requestDto 아이디 찾기 요청 DTO
     * @return 사용자 ID
     */
    String findAdminId(FindIdRequestDto requestDto);

    /**
     * 관리자 비밀번호 찾기 - 임시 비밀번호 생성 및 DB 업데이트
     * @param requestDto 비밀번호 찾기 요청 DTO
     * @return 임시 비밀번호
     */
    String findAdminPasswordAndSendEmail(FindPasswordRequestDto requestDto) throws Exception;

    /**
     * 인증 완료된 사용자에 대해 토큰 발급, 세션 생성, 쿠키 세팅, 감사 로그 기록을 수행
     *
     * @param authenticatedUser 인증된 사용자
     * @param request           HTTP 요청
     * @param response          HTTP 응답
     * @param loginSessionId    로그인 세션 ID (null이면 익명 쿠키에서 계승 또는 새로 생성)
     * @return LoginResponseDto
     */
    LoginResponseDto issueTokensAndCreateSession(
            BaseUser authenticatedUser,
            HttpServletRequest request,
            HttpServletResponse response,
            String loginSessionId
    );

}