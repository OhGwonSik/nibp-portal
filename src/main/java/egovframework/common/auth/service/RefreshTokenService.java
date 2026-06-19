package egovframework.common.auth.service;

/**
 * @ClassName : RefreshTokenService.java
 * @Description : Refresh Token 서비스 인터페이스
 *
 * @author : tspark
 * @since  : 2025. 11. 04
 * @version : 1.0
 */
public interface RefreshTokenService {

    /**
     * Refresh Token 생성 및 저장
     * @param userId 사용자 ID
     * @param userRole 사용자 역할 (USER, ADMIN)
     * @return String Refresh Token
     */
    String createRefreshToken(String userId, String userRole);

    /**
     * Refresh Token 생성 및 저장 (loginSessionId 포함)
     * @param userId 사용자 ID
     * @param userRole 사용자 역할 (USER, ADMIN)
     * @param loginSessionId 로그인 세션 ID (UUID)
     * @return String Refresh Token
     */
    String createRefreshToken(String userId, String userRole, String loginSessionId);

    /**
     * Refresh Token 검증
     * @param token Refresh Token
     * @return boolean 유효 여부
     */
    boolean validateRefreshToken(String token);

    /**
     * Refresh Token으로 사용자 ID 조회
     * @param token Refresh Token
     * @return String 사용자 ID
     */
    String getUserIdFromToken(String token);

    /**
     * Refresh Token 회전 (Rotation)
     * @param oldToken 기존 Refresh Token
     * @return String 새로운 Refresh Token
     */
    String rotateRefreshToken(String oldToken);

    /**
     * Refresh Token 폐기
     * @param token Refresh Token
     */
    void revokeToken(String token);

    /**
     * 사용자의 모든 Refresh Token 폐기
     * @param userId 사용자 ID
     */
    void revokeAllTokens(String userId);

    /**
     * 만료된 토큰 정리
     */
    void cleanupExpiredTokens();
}
