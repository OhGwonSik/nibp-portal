package egovframework.common.audit.service.impl;

import egovframework.admin.admin800.domain.Admin803AuthDTO;
import egovframework.admin.admin800.mapper.Admin803Mapper;
import egovframework.common.audit.domain.*;
import egovframework.common.audit.dto.UpdateLogoutInfoDto;
import egovframework.common.audit.enums.LoginResult;
import egovframework.common.audit.mapper.*;
import egovframework.common.audit.service.AuditService;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.auth.mapper.AuthMapper;
import egovframework.common.component.AESComponent;
import egovframework.common.constant.Constants;
import egovframework.common.util.CryptoUtil;
import egovframework.common.util.RequestUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl extends EgovAbstractServiceImpl implements AuditService {
    private final AuditMapper auditMapper;
    private final AccessLogMapper accessLogMapper; // AccessLogMapper 주입
    private final PermissionChangeLogMapper permissionChangeLogMapper; // PermissionChangeLogMapper 주입
    private final PersonalInfoProcLogMapper personalInfoProcLogMapper; // PersonalInfoProcLogMapper 주입
    private final CryptoUtil cryptoUtil; // 암호화
    private final AESComponent aesComponent;
    private final AuditLogAsyncWriter auditLogAsyncWriter; // 비동기 로그 작성 위임
    private final Admin803Mapper admin803Mapper; // Admin803Mapper 주입
    private final AuthMapper authMapper; // AuthMapper 주입 (순환 참조 방지를 위해 직접 Mapper 사용)

    @Override
    public void logLoginAttempt(HttpServletRequest request, String attemptedUserId, BaseUser user, LoginResult result, String lgnFailRsn, String loginSsnId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String ipAddress = RequestUtil.getRemoteIpAddress();

            LoginLog.LoginLogBuilder logBuilder = LoginLog.builder()
                .userId(user != null ? user.getUserId() : "ANONYMOUS") // 시도한 ID를 기록
                .lgnDt(now)
                .lgnRslt(result)
                .lgnFailRsn(lgnFailRsn)
                .ssnId(loginSsnId) // 로그인 세션 ID 추가
                .ipAddr(ipAddress) // IP 주소
                .userAgt(RequestUtil.getUserAgent())
                .brwsrNm(RequestUtil.getBrowserName())
                .brwsrVer(RequestUtil.getBrowserVersion())
                .osNm(RequestUtil.getOs())
                .osVer(RequestUtil.getOsVersion())
                .dvcType(RequestUtil.getDeviceType())
                .regDt(now);

            if (user != null) {
                String userId = user.getUserId();
                logBuilder.userOid(user.getUserOid())
                          .regId(userId != null && userId.length() > 10 ? userId.substring(0, 10) : userId);
            } else {
                // 사용자를 찾지 못한 경우에도 시도한 ID를 기록(정보가 존재하지 않을 시)
                String regId = attemptedUserId != null ? attemptedUserId : "ANONYMOUS";
                logBuilder.regId(regId.length() > 10 ? regId.substring(0, 10) : regId);
            }

            auditLogAsyncWriter.insertLoginLog(cryptoUtil.encrypt(logBuilder.build()));
        } catch (Exception e) {
            log.error("Failed to insert login log for user: {}", attemptedUserId, e);
        }
    }

    // 로그인 로그 생성 - AuditLogAsyncWriter로 위임
    @Override
    public void insertLoginLog(LoginLog loginLog) {
        auditLogAsyncWriter.insertLoginLog(loginLog);
    }

    // 권한 변경 로그 생성
    // 현재 권한 변경은 한 페이지에서 따로 호출하여 사용x
    @Override
    public int insertChangeAuthLog(BaseUser user) {
        // return auditMapper.insertChangeAuthLog(user);
        return 0;
    }

    // 토큰 만료 시간이 지난 로그 로그아웃 기록 처리 (Role별)
    @Override
    public int updateLoginLogForExpiredToken(LocalDateTime baseTime, String userRole) {
        return auditMapper.updateLoginLogForExpiredToken(baseTime, userRole);
    }

    // 로그인 실패 횟수 증가
    @Override
    public void recordLoginFailure(String userId) {
        try {
            auditMapper.updateLgnFailCnt(userId);
        } catch (Exception e) {
            log.error("Failed to record login failure for user: {}", userId, e);
        }
    }

    // 로그인 성공시 정보 업데이트
    // 로그인 실패 횟수 초기화 및 마지막 로그인 시간 업데이트
    @Override
    public void processLoginSuccess(String userId) {
        try {
            auditMapper.updateUserOnLoginSuccess(userId);
        } catch (Exception e) {
            log.error("Failed to process login success for user: {}", userId, e);
        }
    }

    @Override
    public void updateLogoutInfo(UpdateLogoutInfoDto dto) {
        LocalDateTime lgtDt = LocalDateTime.now();
        try {
            int updatedRows = auditMapper.updateLogoutInfo(dto, lgtDt);
            if (updatedRows > 0) {
                log.info("Logout info updated for user: {}, loginSessionId: {}", dto.userId(), dto.loginSsnId());
            } else {
                log.warn("No active login session found to update logout info for user: {}, loginSessionId: {}", dto.userId(), dto.loginSsnId());
            }
        } catch (Exception e) {
            log.error("Failed to update logout info for user: {}", dto.userId(), e);
        }
    }

    @Override
    @Async("asyncExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertAccessLog(ApiAccessLog apiAccessLog) {
        try {
            int result = accessLogMapper.insertAccessLog(apiAccessLog);
            if(result == 0) {
                log.error("Failed to insert access log for user: {}", apiAccessLog.getUserId());
            }
        } catch (Exception e) {
            log.error("Failed to insert access log for user: {}", apiAccessLog.getUserId(), e);
        }
    }

    @Override
    @Async("asyncExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPermissionChanges(List<PermissionChangeLog> permissionChangeLogs) {
        try {
            int result = permissionChangeLogMapper.insertPermissionChangeLog(permissionChangeLogs);
            if(result == 0) {
                permissionChangeLogs.forEach(permissionChangeLog -> {
                    log.error("Failed to insert permission change log for target user: {}, menu: {}", permissionChangeLog.getTrgtUserId(), permissionChangeLog.getMenuOid());
                });
            } else {
                log.info("permission log insert success");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Async("asyncExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertPersonalInfoProcLog(PersonalInfoProcLog personalInfoProcLog) {
        try {
            int result = personalInfoProcLogMapper.insertPersonalInfoProcLog(personalInfoProcLog);
            if(result == 0) {
                log.error("Failed to insert personal info proc log for user: {}", personalInfoProcLog.getAcsId());
            }
        } catch (Exception e) {
            log.error("Failed to insert personal info proc log for user: {}", personalInfoProcLog.getAcsId(), e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deactivateExpiredMenuAuthAndLog() {
        try {
            // 1. 만료된 권한 조회
            List<Admin803AuthDTO> expiredAuthList = admin803Mapper.selectExpiredMenuAuth();

            if (expiredAuthList.isEmpty()) {
                log.info("No expired menu auth found");
                return 0;
            }

            log.info("Found {} expired menu auth", expiredAuthList.size());

            // 2. 만료된 권한 비활성화
            int deactivatedCount = admin803Mapper.deactivateExpiredMenuAuth();

            if (deactivatedCount == 0) {
                log.warn("No menu auth deactivated");
                return 0;
            }

            log.info("Deactivated {} menu auth", deactivatedCount);

            // 3. 권한 변경 로그 생성
            java.util.List<PermissionChangeLog> permissionChangeLogs = new java.util.ArrayList<>();

            for (Admin803AuthDTO expiredAuth : expiredAuthList) {
                // 사용자 정보 조회 (순환 참조 방지를 위해 Mapper 직접 사용)
                String secretKey = aesComponent.getSecretKey();
                BaseUser targetUser = authMapper.selectUserByUserOid(expiredAuth.getUserOid(), secretKey);
                String trgtUserId = targetUser != null ? targetUser.getUserId() : "UNKNOWN";

                // 각 권한별로 REMOVE 로그 생성
                addPermissionChangeLog(permissionChangeLogs, Constants.SYSTEM_ID, trgtUserId, expiredAuth.getUserMenuAuthrtOid(),
                    expiredAuth.getMenuOid(), "USE_YN", expiredAuth.getUseYn(), "N", "REMOVE", "권한 만료");
                addPermissionChangeLog(permissionChangeLogs, Constants.SYSTEM_ID, trgtUserId, expiredAuth.getUserMenuAuthrtOid(),
                    expiredAuth.getMenuOid(), "READ_YN", expiredAuth.getInqAuthrtYn(), "N", "REMOVE", "권한 만료");
                addPermissionChangeLog(permissionChangeLogs, Constants.SYSTEM_ID, trgtUserId, expiredAuth.getUserMenuAuthrtOid(),
                    expiredAuth.getMenuOid(), "WRITE_YN", expiredAuth.getWrtAuthrtYn(), "N", "REMOVE", "권한 만료");
                addPermissionChangeLog(permissionChangeLogs, Constants.SYSTEM_ID, trgtUserId, expiredAuth.getUserMenuAuthrtOid(),
                    expiredAuth.getMenuOid(), "DELETE_YN", expiredAuth.getDelAuthrtYn(), "N", "REMOVE", "권한 만료");
                addPermissionChangeLog(permissionChangeLogs, Constants.SYSTEM_ID, trgtUserId, expiredAuth.getUserMenuAuthrtOid(),
                    expiredAuth.getMenuOid(), "EXCEL_YN", expiredAuth.getExcelAuthrtYn(), "N", "REMOVE", "권한 만료");
                addPermissionChangeLog(permissionChangeLogs, Constants.SYSTEM_ID, trgtUserId, expiredAuth.getUserMenuAuthrtOid(),
                    expiredAuth.getMenuOid(), "PRINT_YN", expiredAuth.getOtptAuthrtYn(), "N", "REMOVE", "권한 만료");
            }

            // 4. 로그 일괄 저장
            if (!permissionChangeLogs.isEmpty()) {
                logPermissionChanges(permissionChangeLogs);
                log.info("Saved {} permission change logs", permissionChangeLogs.size());
            }

            return deactivatedCount;
        } catch (Exception e) {
            log.error("Failed to deactivate expired menu auth", e);
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deactivateExpiredPrivacyAuth() {
        try {
            // 1. 만료된 개인정보 처리권한 대상 조회
            List<BaseUser> expiredList = auditMapper.selectExpiredPrivacyAuth();

            if (expiredList.isEmpty()) {
                log.info("No expired privacy auth found");
                return 0;
            }

            log.info("Found {} expired privacy auth", expiredList.size());

            // 2. 만료된 개인정보 처리권한 비활성화
            int deactivatedCount = auditMapper.deactivateExpiredPrivacyAuth();

            if (deactivatedCount == 0) {
                log.warn("No privacy auth deactivated");
                return 0;
            }

            log.info("Deactivated {} privacy auth", deactivatedCount);

            // 3. 권한 변경 로그 생성
            List<PermissionChangeLog> permissionChangeLogs = new java.util.ArrayList<>();

            for (BaseUser expiredUser : expiredList) {
                addPermissionChangeLog(permissionChangeLogs, Constants.SYSTEM_ID, expiredUser.getUserId(), null,
                        null, "PRVC_USE_YN", expiredUser.getPrvcUseYn(), "N", "REMOVE", "개인정보 처리권한 만료");
            }

            // 4. 로그 일괄 저장
            if (!permissionChangeLogs.isEmpty()) {
                logPermissionChanges(permissionChangeLogs);
                log.info("Saved {} privacy permission change logs", permissionChangeLogs.size());
            }

            return deactivatedCount;
        } catch (Exception e) {
            log.error("Failed to deactivate expired privacy auth", e);
            throw e;
        }
    }

    /**
     * 권한 변경 로그를 리스트에 추가하는 헬퍼 메서드
     */
    private void addPermissionChangeLog(List<PermissionChangeLog> logs, String chnrgUserId, String trgtUserId,
            Long userMenuAuthrtOid, Long menuOid, String prmsnType, String oldVl, String newVl,
            String chgType, String rsn) {
        if (java.util.Objects.equals(oldVl, newVl) && !chgType.equals("ADD") && !chgType.equals("REMOVE")) {
            return; // 값이 변경되지 않았으면 로그 기록 안함
        }
        PermissionChangeLog log = PermissionChangeLog.builder()
                .chnrgUserId(chnrgUserId)
                .trgtUserId(trgtUserId)
                .userMenuAuthrtOid(userMenuAuthrtOid)
                .menuOid(menuOid)
                .prmsnType(prmsnType)
                .oldVl(oldVl)
                .newVl(newVl)
                .chgType(chgType)
                .chgDt(LocalDateTime.now())
                .rsn(rsn)
                .regDt(LocalDateTime.now())
                .regId(chnrgUserId)
                .build();
        logs.add(log);
    }
}