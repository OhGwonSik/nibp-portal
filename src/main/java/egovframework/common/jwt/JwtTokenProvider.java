package egovframework.common.jwt;

import egovframework.common.auth.domain.BaseUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName : JwtTokenProvider.java
 * @Description : JWT 토큰 생성 및 검증
 *
 * @author : tspark
 * @since  : 2025. 10. 29
 * @version : 1.0
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일              수정자               수정내용
 *  -------------  ------------   ---------------------
 *   2025. 10. 29    tspark               최초 생성
 * </pre>
 *
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:your-256-bit-secret-key-change-this-in-production-environment!!}")
    private String secretKeyString;

    // Role별 Access Token 만료 시간
    @Value("${jwt.access-token.expiration.user:10800000}")
    private long userAccessTokenExpiration;  // 1일

    @Value("${jwt.access-token.expiration.admin:1800000}")
    private long adminAccessTokenExpiration;  // 30분

    // Role별 Refresh Token 만료 시간
    @Value("${jwt.refresh-token.expiration.user:604800000}")
    private long userRefreshTokenExpiration;  // 7일

    @Value("${jwt.refresh-token.expiration.admin:86400000}")
    private long adminRefreshTokenExpiration;  // 1일


    /**
     * HMAC-SHA512 서명 키 생성
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access Token 생성
     *
     * @param user 사용자 정보
     * @param loginSessionId 로그인 세션 ID (UUID)
     * @return JWT Access Token
     */
    public String generateAccessToken(BaseUser user, String loginSessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("userNm", user.getUserNmKorn());
        claims.put("email", user.getEmlLcal() + "@" + user.getEmlDmn());
        claims.put("type", "access");
        claims.put("userAuthrt", user.getUserAuthrt());
        claims.put("loginSessionId", loginSessionId);  // 로그인 세션 ID

        long expiration = getAccessTokenExpiration(user.getUserAuthrt());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUserId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Refresh Token 생성
     *
     * @param userId 사용자 ID
     * @param tokenId 토큰 고유 ID (Revoke용)
     * @param userAuthrt 사용자 역할 (USER, ADMIN)
     * @param loginSessionId 로그인 세션 ID (UUID)
     * @return JWT Refresh Token
     */
    public String generateRefreshToken(String userId, String tokenId, String userAuthrt, String loginSessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("jti", tokenId); // Token ID for revocation
        claims.put("userAuthrt", userAuthrt); // 사용자 역할
        claims.put("loginSessionId", loginSessionId);  // 로그인 세션 ID

        long expiration = getRefreshTokenExpiration(userAuthrt);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Role별 Access Token 만료 시간 조회
     */
    public long getAccessTokenExpiration(String userAuthrt) {
        return "ADMIN".equals(userAuthrt) ? adminAccessTokenExpiration : userAccessTokenExpiration;
    }

    /**
     * Role별 Refresh Token 만료 시간 조회
     */
    public long getRefreshTokenExpiration(String userAuthrt) {
        return "ADMIN".equals(userAuthrt) ? adminRefreshTokenExpiration : userRefreshTokenExpiration;
    }


    /**
     * 토큰에서 Claims 추출
     *
     * @param token JWT 토큰
     * @return Claims
     * @throws JwtException 토큰 파싱 실패 시
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.warn("Invalid token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 토큰에서 사용자 ID 추출
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 토큰에서 특정 클레임 추출
     *
     * @param token JWT 토큰
     * @param claimKey 클레임 키
     * @return 클레임 값
     */
    public String getClaimValue(String token, String claimKey) {
        return getClaims(token).get(claimKey, String.class);
    }

    /**
     * 토큰 타입 확인
     *
     * @param token JWT 토큰
     * @return 토큰 타입 (access, refresh, temp)
     */
    public String getTokenType(String token) {
        return getClaims(token).get("type", String.class);
    }

    /**
     * Token ID 추출 (Refresh Token용)
     *
     * @param token JWT 토큰
     * @return Token ID
     */
    public String getTokenId(String token) {
        return getClaims(token).get("jti", String.class);
    }

    /**
     * 로그인 세션 ID 추출
     *
     * @param token JWT 토큰
     * @return 로그인 세션 ID (UUID)
     */
    public String getLoginSessionId(String token) {
        if (token == null) {
            return null;
        }
        return getClaims(token).get("loginSessionId", String.class);
    }

    /**
     * 토큰 검증
     *
     * @param token JWT 토큰
     * @param expectedType 기대하는 토큰 타입 (access, refresh, temp)
     * @return 유효 여부
     */
    public boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = getClaims(token);

            // 1. 타입 확인
            String type = claims.get("type", String.class);
            if (expectedType != null && !expectedType.equals(type)) {
                log.warn("Token type mismatch. Expected: {}, Actual: {}", expectedType, type);
                return false;
            }

            // 2. 만료 확인
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                log.debug("Token expired at: {}", expiration);
                return false;
            }

            return true;

        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("Invalid token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Token validation error", e);
            return false;
        }
    }

    /**
     * 토큰 만료 시간 확인
     *
     * @param token JWT 토큰
     * @return 만료 시간
     */
    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * 토큰 만료까지 남은 시간 (초)
     *
     * @param token JWT 토큰
     * @return 남은 시간 (초)
     */
    public long getRemainingSeconds(String token) {
        Date expiration = getExpirationDate(token);
        long remainingMs = expiration.getTime() - System.currentTimeMillis();
        return remainingMs / 1000;
    }

    /**
     * Access Token에서 User 객체 복원
     *
     * @param token Access Token
     * @return User 객체 (비밀번호 제외)
     */
    public BaseUser extractUser(String token) {
        try {
            Claims claims = getClaims(token);
            String userAuthrt = claims.get("userAuthrt", String.class);
            String email = claims.get("email", String.class);
            String[] emailParts = email != null ? email.split("@") : new String[]{"", ""};

            return BaseUser.builder()
                    .userId(claims.getSubject())
                    .userNmKorn(claims.get("userNm", String.class))
                    .emlLcal(emailParts.length > 0 ? emailParts[0] : "")
                    .emlDmn(emailParts.length > 1 ? emailParts[1] : "")
                    .userAuthrt(userAuthrt)
                    .build();

        } catch (Exception e) {
            log.error("Failed to extract user from token", e);
            return null;
        }
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     *
     * @param authorizationHeader Authorization 헤더 값
     * @return JWT 토큰 (Bearer 제거)
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7);
    }

    /**
     * 토큰이 곧 만료되는지 확인
     *
     * @param token JWT 토큰
     * @param thresholdSeconds 임계값 (초)
     * @return 만료 임박 여부
     */
    public boolean isTokenExpiringSoon(String token, long thresholdSeconds) {
        long remainingSeconds = getRemainingSeconds(token);
        return remainingSeconds <= thresholdSeconds;
    }

    /**
     * 로그인 세션 ID 생성 (UUID 기반)
     * 하나의 로그인 세션을 식별하기 위한 고유 ID 생성
     *
     * @return UUID 형식의 로그인 세션 ID
     */
    public String generateLoginSessionId() {
        return UUID.randomUUID().toString();
    }
}
