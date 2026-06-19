package egovframework.common.audit.schedule;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import egovframework.common.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditScheduler {
    private final AuditService auditService;

    // Role별 Access Token 만료 시간 (밀리초)
    @Value("${jwt.access-token.expiration.admin:1800000}")
    private long adminAccessTokenExpiration; // 30분

    @Value("${jwt.access-token.expiration.user:10800000}")
    private long userAccessTokenExpiration; // 3시간

    // 토큰 만료 시점에 따른 로그인 로그 로그아웃 업데이트 (Role별 분리 처리)
    @Scheduled(cron = "0 0 3 * * ?") // 새벽 3시
    public void updateLoginLogForExpiredToken() {
        log.warn("==============Scheduler updateLoginLogForExpiredToken start==============");

        try {
            // ADMIN: 30분 기준
            LocalDateTime adminBaseTime = LocalDateTime.now().minusSeconds(adminAccessTokenExpiration / 1000);
            int adminCount = auditService.updateLoginLogForExpiredToken(adminBaseTime, "ADMIN");
            log.info("Updated {} ADMIN login logs for expired token (baseTime: {})", adminCount, adminBaseTime);

            // USER: 3시간 기준
            LocalDateTime userBaseTime = LocalDateTime.now().minusSeconds(userAccessTokenExpiration / 1000);
            int userCount = auditService.updateLoginLogForExpiredToken(userBaseTime, "USER");
            log.info("Updated {} USER login logs for expired token (baseTime: {})", userCount, userBaseTime);

        } catch (Exception e) {
            log.error("Scheduler error : updateLoginLogForExpiredToken error", e);
        }
        log.warn("==============Scheduler updateLoginLogForExpiredToken end==============");
    }

    // 만료된 개인정보 처리권한 비활성화
    @Scheduled(cron = "0 30 2 * * ?") // 새벽 2시 30분
    public void deactivateExpiredPrivacyAuth() {
        log.warn("==============Scheduler deactivateExpiredPrivacyAuth start==============");
        try {
            int deactivatedCount = auditService.deactivateExpiredPrivacyAuth();
            log.info("Deactivated {} expired privacy auth", deactivatedCount);
        } catch (Exception e) {
            log.error("Scheduler error : deactivateExpiredPrivacyAuth error", e);
        }
        log.warn("==============Scheduler deactivateExpiredPrivacyAuth end==============");
    }

    // 만료된 메뉴 권한 비활성화 및 로그 기록
    // @Scheduled(cron = "0 0/3 * * * ?") // 3분마다 실행 (테스트용)
    @Scheduled(cron = "0 30 2 * * ?") // 새벽 2시 30분 (운영용)
    public void deactivateExpiredMenuAuth() {
        log.warn("==============Scheduler deactivateExpiredMenuAuth start==============");
        try {
            int deactivatedCount = auditService.deactivateExpiredMenuAuthAndLog();
            log.info("Deactivated {} expired menu auth", deactivatedCount);
        } catch (Exception e) {
            log.error("Scheduler error : deactivateExpiredMenuAuth error", e);
        }
        log.warn("==============Scheduler deactivateExpiredMenuAuth end==============");
    }
}