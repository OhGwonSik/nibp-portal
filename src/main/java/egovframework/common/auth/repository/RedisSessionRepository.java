package egovframework.common.auth.repository;

import egovframework.common.auth.domain.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis 세션 관리 Repository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.redis.key-prefix:portal}")
    private String keyPrefix;

    // Role별 Access Token 만료 시간 (= 세션 TTL)
    @Value("${jwt.access-token.expiration.user:10800000}")
    private long userAccessTokenExpirationMs;  // 1일

    @Value("${jwt.access-token.expiration.admin:1800000}")
    private long adminAccessTokenExpirationMs;  // 30분

    // Role별 Refresh Token 만료 시간
    @Value("${jwt.refresh-token.expiration.user:604800000}")
    private long userRefreshTokenExpirationMs;  // 7일

    @Value("${jwt.refresh-token.expiration.admin:86400000}")
    private long adminRefreshTokenExpirationMs;  // 1일

    private static final String USER_SESSION_KEY = "user:session:";
    private static final String LOGIN_SESSION_KEY = "loginSession:";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 세션 관리 ====================

    /**
     * 사용자 세션 조회
     */
    public Optional<UserSession> getSession(String userId) {
        String key = buildKey(USER_SESSION_KEY + userId);
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(mapToUserSession(entries));
        } catch (Exception e) {
            log.error("Redis 세션 조회 실패: userId={}", userId, e);
            return Optional.empty();
        }
    }

    /**
     * 사용자 세션 저장
     * TTL = Access Token 만료 시간 (세션 타임아웃), Role별 다름
     */
    public void saveSession(String userId, UserSession session) {
        String key = buildKey(USER_SESSION_KEY + userId);
        String userRole = session.getUserRole();
        long ttl = getAccessTokenTtl(userRole);

        try {
            Map<String, String> map = sessionToMap(session);
            redisTemplate.opsForHash().putAll(key, map);
            redisTemplate.expire(key, ttl, TimeUnit.MILLISECONDS);

            // loginSessionId 역매핑 저장
            if (session.getLoginSessionId() != null) {
                String loginSessionKey = buildKey(LOGIN_SESSION_KEY + session.getLoginSessionId());
                redisTemplate.opsForValue().set(loginSessionKey, userId, ttl, TimeUnit.MILLISECONDS);
            }

            log.debug("Redis 세션 저장: userId={}, userRole={}, loginSessionId={}, ttl={}ms", userId, userRole, session.getLoginSessionId(), ttl);
        } catch (Exception e) {
            log.error("Redis 세션 저장 실패: userId={}", userId, e);
        }
    }

    /**
     * 세션 TTL 갱신 (토큰 갱신 시 호출)
     * Access Token 갱신 시 세션 타임아웃도 리셋
     */
    public void refreshSessionTtl(String userId, String userRole) {
        String key = buildKey(USER_SESSION_KEY + userId);
        long ttl = getAccessTokenTtl(userRole);

        try {
            Boolean exists = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(exists)) {
                redisTemplate.expire(key, ttl, TimeUnit.MILLISECONDS);

                // loginSessionId 역매핑 TTL도 갱신
                Optional<UserSession> session = getSession(userId);
                session.ifPresent(s -> {
                    if (s.getLoginSessionId() != null) {
                        String loginSessionKey = buildKey(LOGIN_SESSION_KEY + s.getLoginSessionId());
                        redisTemplate.expire(loginSessionKey, ttl, TimeUnit.MILLISECONDS);
                    }
                });

                log.debug("Redis 세션 TTL 갱신: userId={}, userRole={}, ttl={}ms", userId, userRole, ttl);
            }
        } catch (Exception e) {
            log.error("Redis 세션 TTL 갱신 실패: userId={}", userId, e);
        }
    }

    /**
     * 세션 정보 업데이트 (토큰 갱신 시 새 토큰 정보로 업데이트)
     */
    public void updateSession(String userId, String userRole, String newAccessTokenHash, String newRefreshTokenId) {
        String key = buildKey(USER_SESSION_KEY + userId);
        long ttl = getAccessTokenTtl(userRole);

        try {
            Boolean exists = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(exists)) {
                redisTemplate.opsForHash().put(key, "accessTokenHash", newAccessTokenHash);
                redisTemplate.opsForHash().put(key, "refreshTokenId", newRefreshTokenId);
                redisTemplate.expire(key, ttl, TimeUnit.MILLISECONDS);

                log.debug("Redis 세션 업데이트: userId={}, userRole={}", userId, userRole);
            }
        } catch (Exception e) {
            log.error("Redis 세션 업데이트 실패: userId={}", userId, e);
        }
    }

    // ==================== TTL 조회 ====================

    /**
     * Role별 Access Token 만료 시간 (= 세션 TTL) 조회
     */
    public long getAccessTokenTtl(String userRole) {
        return "ADMIN".equals(userRole) ? adminAccessTokenExpirationMs : userAccessTokenExpirationMs;
    }

    /**
     * Role별 Refresh Token 만료 시간 조회
     */
    public long getRefreshTokenTtl(String userRole) {
        return "ADMIN".equals(userRole) ? adminRefreshTokenExpirationMs : userRefreshTokenExpirationMs;
    }

    /**
     * 사용자 세션 삭제
     */
    public void deleteSession(String userId) {
        String key = buildKey(USER_SESSION_KEY + userId);
        try {
            // 기존 세션에서 loginSessionId 조회 후 삭제
            Optional<UserSession> session = getSession(userId);
            session.ifPresent(s -> {
                if (s.getLoginSessionId() != null) {
                    String loginSessionKey = buildKey(LOGIN_SESSION_KEY + s.getLoginSessionId());
                    redisTemplate.delete(loginSessionKey);
                }
            });

            redisTemplate.delete(key);
            log.debug("Redis 세션 삭제: userId={}", userId);
        } catch (Exception e) {
            log.error("Redis 세션 삭제 실패: userId={}", userId, e);
        }
    }

    // ==================== 유틸리티 ====================

    /**
     * 토큰 해시 생성 (SHA-256)
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다", e);
        }
    }

    /**
     * Key prefix 적용
     */
    private String buildKey(String key) {
        return keyPrefix + ":" + key;
    }

    /**
     * Map -> UserSession 변환
     */
    private UserSession mapToUserSession(Map<Object, Object> map) {
        return UserSession.builder()
                .userId((String) map.get("userId"))
                .userRole((String) map.get("userRole"))
                .loginSessionId((String) map.get("loginSessionId"))
                .accessTokenHash((String) map.get("accessTokenHash"))
                .refreshTokenId((String) map.get("refreshTokenId"))
                .loginAt(parseDateTime((String) map.get("loginAt")))
                .ipAddress((String) map.get("ipAddress"))
                .userAgent((String) map.get("userAgent"))
                .build();
    }

    /**
     * UserSession -> Map 변환
     */
    private Map<String, String> sessionToMap(UserSession session) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", session.getUserId());
        map.put("userRole", session.getUserRole());
        map.put("loginSessionId", session.getLoginSessionId());
        map.put("accessTokenHash", session.getAccessTokenHash());
        map.put("refreshTokenId", session.getRefreshTokenId());
        map.put("loginAt", formatDateTime(session.getLoginAt()));
        map.put("ipAddress", session.getIpAddress());
        map.put("userAgent", session.getUserAgent());
        return map;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER) : null;
    }
}
