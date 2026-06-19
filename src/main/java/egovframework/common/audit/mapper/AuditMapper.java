package egovframework.common.audit.mapper;

import egovframework.common.audit.domain.ApiAccessLog;
import egovframework.common.audit.domain.LoginLog;
import egovframework.common.audit.dto.UpdateLogoutInfoDto;
import egovframework.common.auth.domain.BaseUser;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AuditMapper {
    int insertLoginLog(LoginLog loginLog); // 로그인 로그
    int insertChangeAuthLog(BaseUser user); // 권한 변경 로그
    int updateLoginLogForExpiredToken(@Param("baseTime") LocalDateTime baseTime, @Param("userRole") String userRole); // 토큰 만료 시점에 따른 로그인 로그 업데이트 (Role별)

    // ===== 접근 로그 (API + 페이지) =====
    /**
     * 접근 로그 저장 (API, 페이지)
     * @param apiAccessLog
     * @return
     */
    int insertAccessLog(ApiAccessLog apiAccessLog);

    /**
     * 로그인 실패 횟수 초기화
     * @param userId
     * @return
     */
    int resetLgnFailCnt(String userId);

    /**
     * 로그인 실패 횟수 증가
     * @param userId
     * @return
     */
    int updateLgnFailCnt(String userId);

    /**
     * 로그인 성공시 정보 업데이트
     * @param userId
     * @return
     */
    int updateUserOnLoginSuccess(String userId);

    /**
     * 로그아웃 정보 업데이트
     * @param dto 로그아웃 정보 DTO
     * @param lgtDt 로그아웃 시간
     * @return
     */
    int updateLogoutInfo(@Param("dto") UpdateLogoutInfoDto dto, @Param("lgtDt") LocalDateTime lgtDt);

    /**
     * 만료된 개인정보 처리권한 대상 조회
     * @return 만료 대상 사용자 목록
     */
    List<BaseUser> selectExpiredPrivacyAuth();

    /**
     * 만료된 개인정보 처리권한 비활성화
     * @return 비활성화된 건수
     */
    int deactivateExpiredPrivacyAuth();

}
