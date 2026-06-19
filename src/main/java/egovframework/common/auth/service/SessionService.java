package egovframework.common.auth.service;

import egovframework.common.auth.domain.BaseUser;
import egovframework.common.auth.domain.UserSession;

import java.util.Optional;

/**
 * @ClassName : SessionService.java
 * @Description : Redis 세션 관리 서비스
 *
 * @author : t.s.park
 * @since  : 2025. 02. 05
 * @version : 1.0
 */
public interface SessionService {

    /**
     * 세션 생성
     *
     * @param user 사용자 정보
     * @param accessToken Access Token
     * @param refreshTokenId Refresh Token ID (jti)
     * @param loginSessionId 로그인 세션 ID
     * @param ipAddress 클라이언트 IP
     * @param userAgent 클라이언트 User-Agent
     * @return 생성된 UserSession
     */
    UserSession createSession(BaseUser user, String accessToken, String refreshTokenId,
                              String loginSessionId, String ipAddress, String userAgent);

    /**
     * 세션 조회
     *
     * @param userId 사용자 ID
     * @return UserSession (없으면 empty)
     */
    Optional<UserSession> getSession(String userId);

    /**
     * 기존 세션 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @return 세션 존재 여부
     */
    boolean hasExistingSession(String userId);

    /**
     * 세션 무효화 (세션 삭제)
     *
     * @param userId 사용자 ID
     */
    void invalidateSession(String userId);

    /**
     * 세션 무효화 (userId로 세션 조회 후 무효화)
     *
     * @param userId 사용자 ID
     */
    void invalidateSessionByUserId(String userId);

    /**
     * 세션 정보 업데이트 (토큰 갱신 시)
     *
     * @param userId 사용자 ID
     * @param userRole 사용자 역할
     * @param newAccessToken 새 Access Token
     * @param newRefreshTokenId 새 Refresh Token ID
     */
    void updateSession(String userId, String userRole, String newAccessToken, String newRefreshTokenId);

    /**
     * 세션 TTL 갱신
     *
     * @param userId 사용자 ID
     * @param userRole 사용자 역할
     */
    void refreshSessionTtl(String userId, String userRole);

    /**
     * 세션 유효성 검증 (세션 존재 + Access Token 해시 일치)
     *
     * @param userId 사용자 ID
     * @param accessToken Access Token
     * @return 유효한 세션이면 true
     */
    boolean validateSession(String userId, String accessToken);

    /**
     * Refresh Token 유효성 검증 (세션 존재 + Refresh Token ID 일치)
     *
     * @param userId 사용자 ID
     * @param refreshTokenId Refresh Token ID (jti)
     * @return 유효한 Refresh Token이면 true
     */
    boolean validateRefreshToken(String userId, String refreshTokenId);
}
