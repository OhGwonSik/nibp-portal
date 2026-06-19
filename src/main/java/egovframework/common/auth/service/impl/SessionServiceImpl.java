package egovframework.common.auth.service.impl;

import egovframework.common.auth.domain.BaseUser;
import egovframework.common.auth.domain.UserSession;
import egovframework.common.auth.repository.RedisSessionRepository;
import egovframework.common.auth.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @ClassName : SessionServiceImpl.java
 * @Description : Redis 세션 관리 서비스 구현체
 *
 * @author : t.s.park
 * @since  : 2025. 02. 05
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl extends EgovAbstractServiceImpl implements SessionService {

    private final RedisSessionRepository redisSessionRepository;

    @Override
    public UserSession createSession(BaseUser user, String accessToken, String refreshTokenId,
                                     String loginSessionId, String ipAddress, String userAgent) {
        String userId = user.getUserId();
        String userRole = user.getUserAuthrt();

        UserSession session = UserSession.builder()
                .userId(userId)
                .userRole(userRole)
                .loginSessionId(loginSessionId)
                .accessTokenHash(redisSessionRepository.hashToken(accessToken))
                .refreshTokenId(refreshTokenId)
                .loginAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        redisSessionRepository.saveSession(userId, session);
        log.info("세션 생성: userId={}, userRole={}, loginSessionId={}", userId, userRole, loginSessionId);

        return session;
    }

    @Override
    public Optional<UserSession> getSession(String userId) {
        return redisSessionRepository.getSession(userId);
    }

    @Override
    public boolean hasExistingSession(String userId) {
        return redisSessionRepository.getSession(userId).isPresent();
    }

    @Override
    public void invalidateSession(String userId) {
        redisSessionRepository.deleteSession(userId);
        log.info("세션 무효화 완료: userId={}", userId);
    }

    @Override
    public void invalidateSessionByUserId(String userId) {
        invalidateSession(userId);
    }

    @Override
    public void updateSession(String userId, String userRole, String newAccessToken, String newRefreshTokenId) {
        String newAccessTokenHash = redisSessionRepository.hashToken(newAccessToken);
        redisSessionRepository.updateSession(userId, userRole, newAccessTokenHash, newRefreshTokenId);
        log.debug("세션 업데이트: userId={}, userRole={}", userId, userRole);
    }

    @Override
    public void refreshSessionTtl(String userId, String userRole) {
        redisSessionRepository.refreshSessionTtl(userId, userRole);
        log.debug("세션 TTL 갱신: userId={}, userRole={}", userId, userRole);
    }

    @Override
    public boolean validateSession(String userId, String accessToken) {
        Optional<UserSession> sessionOpt = redisSessionRepository.getSession(userId);
        if (sessionOpt.isEmpty()) {
            log.debug("세션 검증 실패 - 세션 없음: userId={}", userId);
            return false;
        }

        UserSession session = sessionOpt.get();
        String currentTokenHash = redisSessionRepository.hashToken(accessToken);

        if (!currentTokenHash.equals(session.getAccessTokenHash())) {
            log.debug("세션 검증 실패 - Access Token 해시 불일치: userId={}", userId);
            return false;
        }

        return true;
    }

    @Override
    public boolean validateRefreshToken(String userId, String refreshTokenId) {
        Optional<UserSession> sessionOpt = redisSessionRepository.getSession(userId);
        if (sessionOpt.isEmpty()) {
            log.debug("Refresh Token 검증 실패 - 세션 없음: userId={}", userId);
            return false;
        }

        UserSession session = sessionOpt.get();
        if (!refreshTokenId.equals(session.getRefreshTokenId())) {
            log.debug("Refresh Token 검증 실패 - Token ID 불일치: userId={}", userId);
            return false;
        }

        return true;
    }
}
