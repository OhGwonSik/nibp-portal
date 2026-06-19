package egovframework.common.auth.mapper;

import egovframework.common.auth.domain.*;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * @ClassName : AuthMapper.java
 * @Description : 인증 관련 Mapper
 *
 * @author : tspark
 * @since  : 2025. 11. 04
 * @version : 1.0
 */
@Mapper
public interface AuthMapper {
    /**
     * 사용자 ID로 사용자 정보 조회
     * @param userId 사용자 ID
     * @return BaseUser 사용자 정보
     */
    BaseUser selectUserByUserId(@Param("userId") String userId, @Param("secretKey") String secretKey);

    /**
     * 사용자 ID로 활성 사용자 정보 조회 (use_yn = 'Y')
     * @param userId 사용자 ID
     * @return BaseUser 사용자 정보
     */
    BaseUser selectActiveUserByUserId(@Param("userId") String userId, @Param("secretKey") String secretKey);

    /**
     * 관리자 ID로 사용자 정보 조회
     * @param userId 사용자 ID
     * @return BaseUser 사용자 정보
     */
    BaseUser selectAdminUserByUserId(@Param("userId") String userId, @Param("secretKey") String secretKey);

    /**
     * 이메일로 사용자 정보 조회
     * @param email 이메일
     * @return BaseUser 사용자 정보
     */
    BaseUser selectUserByEmail(@Param("email") String email, @Param("secretKey") String secretKey);

    /**
     * 사용자 번호로 사용자 정보 조회
     * @param userOid 사용자 번호
     * @return BaseUser 사용자 정보
     */
    BaseUser selectUserByUserOid(@Param("userOid") Long userOid, @Param("secretKey") String secretKey);

    /**
     * 사용자 번호로 비밀번호 업데이트
     * @param userOid 사용자 번호
     * @param encodedPassword 암호화된 비밀번호
     * @return 업데이트 결과
     */
    int updatePasswordByUserOid(@Param("userOid") Long userOid, @Param("password") String encodedPassword);

    /**
     * 비밀번호 재설정 verification token 저장
     * @param userId 사용자 ID
     * @param token 토큰 값
     * @param expiryDate 만료일시
     * @return int 저장 결과
     */
    int insertVerificationToken(@Param("userId") String userId, @Param("token") String token, @Param("expiryDate") java.time.LocalDateTime expiryDate);

    /**
     * verification token 조회
     * @param token 토큰 값
     * @return Map<String, Object> 토큰 정보 (userId, token, expiryDate)
     */
    java.util.Map<String, Object> findVerificationToken(@Param("token") String token);

    /**
     * verification token 무효화 (삭제 또는 상태 변경)
     * @param token 토큰 값
     * @return int 처리 결과
     */
    int invalidateVerificationToken(@Param("token") String token);

    /**
     * 비밀번호 재설정 password reset token 저장
     * @param userId 사용자 ID
     * @param token 토큰 값
     * @param expiryDate 만료일시
     * @param verificationToken 연관된 verification token
     * @return int 저장 결과
     */
    int insertPasswordResetToken(@Param("userId") String userId, @Param("token") String token, @Param("expiryDate") java.time.LocalDateTime expiryDate, @Param("verificationToken") String verificationToken);

    /**
     * password reset token 조회
     * @param token 토큰 값
     * @return Map<String, Object> 토큰 정보 (userId, token, expiryDate, verificationToken)
     */
    java.util.Map<String, Object> findPasswordResetToken(@Param("token") String token);

    /**
     * password reset token 무효화 (삭제 또는 상태 변경)
     * @param token 토큰 값
     * @return int 처리 결과
     */
    int invalidatePasswordResetToken(@Param("token") String token);

    /**
     * 사용자 비밀번호 업데이트
     * @param userOid 사용자pk
     * @param encPswd 새 암호화된 비밀번호
     * @return int 처리 결과
     */
    int updateUserPassword(@Param("userOid") Long userOid, @Param("encPswd") String encPswd);

    /**
     * 관리자 등록
     * @param adminUser 사용자 정보
     * @return int 등록 결과
     */
    int insertJoinAdminUser(BaseUser adminUser);

    /**
     * 관리자 아이디 중복 체크
     * @param checkIdRequestDto
     * @return
     */
    int checkAdminId(CheckIdRequestDto checkIdRequestDto);

    /**
     * 유저 이메일 중복 체크
     * @param checkEmailRequestDto
     * @return
     */
    int checkEmail(@Param("request") CheckEmailRequestDto checkEmailRequestDto, @Param("aesKey") String aesKey);

    /**
     * 유저 휴대폰 중복 체크
     * @param checkPhoneRequestDto
     * @return
     */
    int checkPhone(@Param("request") CheckPhoneRequestDto checkPhoneRequestDto, @Param("aesKey") String aesKey);

    /**
     * 관리자 정보 수정
     * @param adminUserUpdateDto 관리자 정보
     * @return int 업데이트 결과
     */
    int updateAdminMe(@Param("request") AdminUserUpdateDto adminUserUpdateDto, @Param("principal") BaseUser principal);

    /**
     * 관리자 비밀번호 수정
     * @param adminUserUpdatePwdDto 관리자 정보
     * @return int 업데이트 결과
     */
    int updateAdminMePswd(@Param("request") AdminUserUpdatePwdDto adminUserUpdatePwdDto, @Param("principal") BaseUser principal);

    /**
     * userId, 이름, 이메일, 휴대폰 번호로 사용자 조회
     * @return int userOid
     */
    Long getUserByNiceCallbackDto(@Param("request") NicePassVerifyCallbackDto requestDto, @Param("secretKey") String secretKey);

    /**
     * 관리자 아이디 찾기 (이름, 이메일, 휴대폰으로 조회)
     * @param requestDto 아이디 찾기 요청 DTO
     * @param secretKey AES 복호화 키
     * @return 사용자 ID
     */
    String findAdminIdByUserInfo(@Param("request") FindIdRequestDto requestDto, @Param("secretKey") String secretKey);

    /**
     * 관리자 비밀번호 찾기 (아이디, 이름, 이메일, 휴대폰으로 조회)
     * @param requestDto 비밀번호 찾기 요청 DTO
     * @param secretKey AES 복호화 키
     * @return 사용자 정보
     */
    BaseUser findAdminUserByPasswordInfo(@Param("request") FindPasswordRequestDto requestDto, @Param("secretKey") String secretKey);
}