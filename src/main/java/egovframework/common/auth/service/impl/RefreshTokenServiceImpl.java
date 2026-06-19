package egovframework.common.auth.service.impl;

import egovframework.common.auth.domain.RefreshToken;
import egovframework.common.auth.mapper.RefreshTokenMapper;
import egovframework.common.auth.service.RefreshTokenService;
import egovframework.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @ClassName : RefreshTokenServiceImpl.java
 * @Description : Refresh Token 서비스 구현체
 *
 * @author : tspark
 * @since  : 2025. 11. 04
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl extends EgovAbstractServiceImpl implements RefreshTokenService {

    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtTokenProvider jwtTokenProvider;

    // Role별 Refresh Token 만료 시간
    @Value("${jwt.refresh-token.expiration.user:604800000}")
    private long userRefreshTokenExpiration;  // 7일

    @Value("${jwt.refresh-token.expiration.admin:86400000}")
    private long adminRefreshTokenExpiration;  // 1일

    @Override
    @Transactional
    public String createRefreshToken(String userId, String userRole) {
        log.debug("Creating refresh token for user: {}", userId);

        // 1. Token ID 생성
        String tokenId = UUID.randomUUID().toString();

        // 2. JWT Refresh Token 생성
        String token = jwtTokenProvider.generateRefreshToken(userId, tokenId, userRole, null);

        // 3. 만료 일시 계산 (Role별 만료 시간)
        long expiration = getRefreshTokenExpiration(userRole);
        LocalDateTime expiryDt = LocalDateTime.now().plusSeconds(expiration / 1000);
        String expiryDtStr = expiryDt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 4. DB에 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(tokenId)
                .userId(userId)
                .token(token)
                .expiryDt(expiryDtStr)
                .build();

        // refreshTokenMapper.insertRefreshToken(refreshToken); // 저장할 시 구현

        log.info("Refresh token created for user: {}", userId);

        return token;
    }

    @Override
    @Transactional
    public String createRefreshToken(String userId, String userRole, String loginSessionId) {
        log.debug("Creating refresh token for user: {} with loginSessionId: {}", userId, loginSessionId);

        // 1. Token ID 생성
        String tokenId = UUID.randomUUID().toString();

        // 2. JWT Refresh Token 생성 (loginSessionId 포함)
        String token = jwtTokenProvider.generateRefreshToken(userId, tokenId, userRole, loginSessionId);

        // 3. 만료 일시 계산 (Role별 만료 시간)
        long expiration = getRefreshTokenExpiration(userRole);
        LocalDateTime expiryDt = LocalDateTime.now().plusSeconds(expiration / 1000);
        String expiryDtStr = expiryDt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 4. DB에 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(tokenId)
                .userId(userId)
                .token(token)
                .expiryDt(expiryDtStr)
                .build();

        // refreshTokenMapper.insertRefreshToken(refreshToken); // 저장할 시 구현

        log.info("Refresh token created for user: {} with loginSessionId: {}", userId, loginSessionId);

        return token;
    }

    /**
     * Role별 Refresh Token 만료 시간 조회
     */
    private long getRefreshTokenExpiration(String userRole) {
        return "ADMIN".equals(userRole) ? adminRefreshTokenExpiration : userRefreshTokenExpiration;
    }

    @Override
    public boolean validateRefreshToken(String token) {
        log.debug("Validating refresh token");

        try {
            // 1. JWT 토큰 검증 (만료 여부도 포함)
            if (!jwtTokenProvider.validateToken(token, "refresh")) {
                log.warn("Invalid JWT refresh token");
                return false;
            }

            // [주석처리] 기존 DB 기반 검증 로직
            // // 2. DB에서 토큰 조회
            // RefreshToken refreshToken = refreshTokenMapper.selectRefreshTokenByToken(token);
            //
            // if (refreshToken == null) {
            //     log.warn("Refresh token not found in database");
            //     return false;
            // }
            //
            // // 3. 폐기 여부 확인
            // if ("Y".equals(refreshToken.getRevokedYn())) {
            //     log.warn("Refresh token has been revoked");
            //     return false;
            // }
            //
            // // 4. 만료 확인
            // LocalDateTime expiryDt = LocalDateTime.parse(refreshToken.getExpiryDt(),
            //         DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            //
            // if (expiryDt.isBefore(LocalDateTime.now())) {
            //     log.warn("Refresh token has expired");
            //     return false;
            // }
            //
            // // 5. 마지막 사용 일시 업데이트
            // refreshTokenMapper.updateLastUsedDt(refreshToken.getTokenId());

            log.debug("Refresh token is valid");
            return true;

        } catch (Exception e) {
            log.error("Error validating refresh token", e);
            return false;
        }
    }

    @Override
    public String getUserIdFromToken(String token) {
        log.debug("Getting user ID from refresh token");

        try {
            // 1. JWT에서 사용자 ID 추출
            String userId = jwtTokenProvider.getUserId(token);

            // 2. DB에서 토큰 검증
            RefreshToken refreshToken = refreshTokenMapper.selectRefreshTokenByToken(token);

            if (refreshToken == null || !"Y".equals(refreshToken.getRevokedYn())) {
                log.warn("Invalid or revoked refresh token");
                return null;
            }

            return userId;

        } catch (Exception e) {
            log.error("Error getting user ID from token", e);
            return null;
        }
    }

    @Override
    @Transactional
    public String rotateRefreshToken(String oldToken) {
        log.debug("Rotating refresh token");

        try {
            // 1. 기존 토큰 검증
            if (!validateRefreshToken(oldToken)) {
                log.warn("Old refresh token is invalid");
                return null;
            }

            // 2. 사용자 ID, userRole, loginSessionId 추출
            String userId = jwtTokenProvider.getUserId(oldToken);
            String userRole = jwtTokenProvider.getClaimValue(oldToken, "userRole");
            String loginSessionId = jwtTokenProvider.getLoginSessionId(oldToken);  // loginSessionId 유지

            // 3. 기존 토큰 폐기
            // RefreshToken oldRefreshToken = refreshTokenMapper.selectRefreshTokenByToken(oldToken);
            // if (oldRefreshToken != null) {
            //     refreshTokenMapper.revokeRefreshToken(oldRefreshToken.getTokenId());
            // }

            // 4. 새로운 토큰 생성 (loginSessionId 유지)
            String newToken = createRefreshToken(userId, userRole, loginSessionId);

            log.info("Refresh token rotated for user: {} with loginSessionId: {}", userId, loginSessionId);

            return newToken;

        } catch (Exception e) {
            log.error("Error rotating refresh token", e);
            return null;
        }
    }

    @Override
    @Transactional
    public void revokeToken(String token) {
        log.debug("Revoking refresh token");

        try {
            RefreshToken refreshToken = refreshTokenMapper.selectRefreshTokenByToken(token);

            if (refreshToken != null) {
                refreshTokenMapper.revokeRefreshToken(refreshToken.getTokenId());
                log.info("Refresh token revoked: {}", refreshToken.getTokenId());
            }

        } catch (Exception e) {
            log.error("Error revoking refresh token", e);
        }
    }

    @Override
    @Transactional
    public void revokeAllTokens(String userId) {
        log.debug("Revoking all refresh tokens for user: {}", userId);

        try {
            int count = refreshTokenMapper.revokeAllTokensByUserId(userId);
            log.info("Revoked {} refresh tokens for user: {}", count, userId);

        } catch (Exception e) {
            log.error("Error revoking all tokens for user: {}", userId, e);
        }
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        log.debug("Cleaning up expired refresh tokens");

        try {
            int count = refreshTokenMapper.deleteExpiredTokens();
            log.info("Deleted {} expired refresh tokens", count);

        } catch (Exception e) {
            log.error("Error cleaning up expired tokens", e);
        }
    }
}
