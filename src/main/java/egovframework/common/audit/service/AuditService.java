package egovframework.common.audit.service;

import egovframework.common.audit.domain.ApiAccessLog;
import egovframework.common.audit.domain.LoginLog;
import egovframework.common.audit.domain.PermissionChangeLog;
import egovframework.common.audit.domain.PersonalInfoProcLog;
import egovframework.common.audit.dto.UpdateLogoutInfoDto;
import egovframework.common.audit.enums.LoginResult;
import egovframework.common.auth.domain.BaseUser;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditService {

    /**
     * 로그인 시도에 대한 로그를 기록합니다. (성공/실패 모두)
     * @param request HttpServletRequest
     * @param attemptedUserId 로그인을 시도한 사용자 ID
     * @param user 조회된 사용자 객체 (없을 경우 null)
     * @param result 로그인 결과
     * @param failReason 실패 사유 (성공 시 null)
     * @param loginSessionId 로그인 세션 ID (추적용)
     */
    void logLoginAttempt(HttpServletRequest request, String attemptedUserId, BaseUser user, LoginResult result, String failReason, String loginSessionId);

    /**
     * 로그인 로그 (비동기 처리)
     * @param loginLog
     */
    void insertLoginLog(LoginLog loginLog);

    /**
     * 권한 변경 로그
     * @param user
     * @return
     */
    int insertChangeAuthLog(BaseUser user);

    /**
     * 토큰 만료 시점에 따른 로그인 로그 업데이트 (Role별)
     * @param baseTime 기준 시간
     * @param userRole 사용자 Role (ADMIN, USER)
     * @return 업데이트된 로그 수
     */
    int updateLoginLogForExpiredToken(LocalDateTime baseTime, String userRole);

    /**
     * 로그인 실패 횟수 증가
     * @param userId
     */
    void recordLoginFailure(String userId);

    /**
     * 로그인 성공 처리 (실패 횟수 초기화 및 마지막 로그인 시간 업데이트)
     * @param userId
     */
    void processLoginSuccess(String userId);

    /**
     * 로그아웃 정보를 업데이트합니다.
     * @param userId 사용자 ID
     * @param userOid 사용자 번호
     * @param loginSessionId 로그인 세션 ID
     */
    void updateLogoutInfo(UpdateLogoutInfoDto dto);

    /**
     * 접근 로그를 기록합니다.
     * @param apiAccessLog 접근 로그 객체
     */
    void insertAccessLog(ApiAccessLog apiAccessLog);

    /**
     * 권한 변경 로그를 기록합니다.
     * @param log 권한 변경 로그 객체
     */
    void logPermissionChanges(List<PermissionChangeLog> permissionChangeLogs);

    /**
     * 개인정보 처리 로그를 기록합니다.
     * @param personalInfoProcLog 개인정보 처리 로그 객체
     */
    void insertPersonalInfoProcLog(PersonalInfoProcLog personalInfoProcLog);

    /**
     * 만료된 메뉴 권한을 비활성화하고 로그를 기록합니다.
     * @return int 비활성화된 권한 수
     */
    int deactivateExpiredMenuAuthAndLog();

    /**
     * 만료된 개인정보 처리권한을 비활성화합니다.
     * @return int 비활성화된 건수
     */
    int deactivateExpiredPrivacyAuth();

}