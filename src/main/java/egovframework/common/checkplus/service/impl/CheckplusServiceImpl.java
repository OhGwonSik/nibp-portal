package egovframework.common.checkplus.service.impl;

import egovframework.common.audit.enums.LoginResult;
import egovframework.common.audit.service.AuditService;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.auth.domain.NicePassVerifyCallbackDto;
import egovframework.common.auth.service.AuthService;
import egovframework.common.auth.service.SessionService;
import egovframework.common.checkplus.service.CheckplusService;
import egovframework.common.component.AESComponent;
import egovframework.common.jwt.JwtTokenProvider;
import egovframework.portal.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckplusServiceImpl extends EgovAbstractServiceImpl implements CheckplusService {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SessionService sessionService;
    private final AuditService auditService;
    private final UserMapper userMapper;
    // private final CryptoUtil cryptoUtil;
    private final AESComponent aesComponent;

    /**
     * 테스트용 본인인증 스킵 처리
     */
    public String handleSkipSuccess(String purpose,
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    HttpSession session,
                                    Model model) {
        log.warn("[CHECKPLUS] ⚠️ SKIP MODE ACTIVATED - purpose: {}", purpose);

        switch (purpose) {
            case "SIGNUP":
                log.info("[CHECKPLUS SKIP] 회원가입 본인인증 스킵");

                // 본인인증 팝업이 열릴 때 사용자가 입력한 값을 받아옴
                // 실제로는 사용자가 폼에 입력한 값을 그대로 사용
                // 여기서는 세션 또는 요청 파라미터로 받을 수 있음

                // 더미 데이터 대신 세션에 검증 완료 표시만 함
                session.setAttribute("signup_phone_verified", true);

                // 클라이언트에서 입력한 값을 그대로 반환하기 위해 빈 값으로 설정
                // JavaScript에서 이미 입력된 값을 유지하도록 함
                model.addAttribute("name", "");
                model.addAttribute("birth", "");
                model.addAttribute("mobileNo", "");

                return "common/modal/checkplus_success_3";

            case "RESET_PWD":
                log.info("[CHECKPLUS SKIP] 비밀번호 재설정 본인인증 스킵");
                // 실제 userId를 세션에서 가져옴
                String resetUserId = (String) session.getAttribute("PWD_RESET_USER_ID");
                if (resetUserId == null) {
                    model.addAttribute("errorMessage", "비밀번호 재설정 대상 아이디가 없습니다.");
                    return "common/modal/checkplus_error";
                }

                // 해당 userId로 사용자 조회
                BaseUser user = authService.getActiveUserById(resetUserId);
                if (user == null) {
                    model.addAttribute("errorMessage", "사용자를 찾을 수 없습니다.");
                    return "common/modal/checkplus_error";
                }

                session.setAttribute("PWD_RESET_VERIFIED", "Y");
                session.setAttribute("PWD_RESET_USER_OID", user.getUserOid());
                return "common/modal/checkplus_success_reset_pwd";

            case "ADMIN_RESET_PWD":
                log.info("[CHECKPLUS SKIP] 관리자 비밀번호 재설정 본인인증 스킵");
                String adminResetUserId = (String) session.getAttribute("ADMIN_PWD_RESET_USER_ID");
                if (adminResetUserId == null) {
                    model.addAttribute("errorMessage", "비밀번호 재설정 대상 아이디가 없습니다.");
                    return "common/modal/checkplus_error";
                }

                // 해당 userId로 사용자 조회
                BaseUser adminResetUser = authService.getActiveUserById(adminResetUserId);
                if (adminResetUser == null) {
                    model.addAttribute("errorMessage", "관리자 계정을 찾을 수 없습니다.");
                    return "common/modal/checkplus_error";
                }

                session.setAttribute("ADMIN_PWD_RESET_VERIFIED", "Y");
                session.setAttribute("ADMIN_PWD_RESET_USER_NO", adminResetUser.getUserOid());
                return "common/modal/checkplus_success_admin_reset_pwd";

            case "ADMIN_LOGIN":
                log.info("[CHECKPLUS SKIP] 관리자 로그인 본인인증 스킵");
                String adminUserId = (String) session.getAttribute("SECOND_AUTH_ADMIN_USER_ID");

                if (adminUserId == null) {
                    model.addAttribute("errorMessage", "관리자 로그인 정보가 존재하지 않습니다.");
                    model.addAttribute("purpose", "ADMIN_LOGIN");
                    return "common/modal/checkplus_error";
                }

                BaseUser adminUser = authService.getActiveUserById(adminUserId);
                if (adminUser == null) {
                    model.addAttribute("errorMessage", "관리자 계정을 찾을 수 없습니다.");
                    model.addAttribute("purpose", "ADMIN_LOGIN");
                    return "common/modal/checkplus_error";
                }

                // 기존 세션 무효화 (단일 로그인 정책)
                if (sessionService.hasExistingSession(adminUserId)) {
                    log.info("Admin login: invalidating existing session for user: {}", adminUserId);
                    sessionService.invalidateSessionByUserId(adminUserId);
                }

                // 토큰 발급 + 쿠키 세팅 + 감사 로그
                String loginSessionId = (String) session.getAttribute("SECOND_AUTH_LOGIN_SESSION_ID");
                authService.issueTokensAndCreateSession(adminUser, request, response, loginSessionId);

                // 세션 정리
                session.removeAttribute("SECOND_AUTH_ADMIN_USER_ID");
                session.removeAttribute("SECOND_AUTH_NAME");
                session.removeAttribute("SECOND_AUTH_BRDT");
                session.removeAttribute("SECOND_AUTH_MPNO_PFX");
                session.removeAttribute("SECOND_AUTH_MPNO_MID");
                session.removeAttribute("SECOND_AUTH_MPNO_SFX");

                model.addAttribute("resultMessage", "관리자 본인인증이 완료되었습니다. (테스트 모드)");
                return "common/modal/checkplus_success_admin_login";
                
            default:
                model.addAttribute("errorMessage", "지원하지 않는 인증 목적입니다.");
                return "common/modal/checkplus_error";
        }
    }

    public String handleSuccess(String purpose,
                                HashMap mapresult,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                HttpSession session,
                                Model model){
        String name     = (String)mapresult.get("NAME");
        String birth    = (String)mapresult.get("BIRTHDATE");
        String mobileNo = (String)mapresult.get("MOBILE_NO");
        log.info("[NICE][SUCCESS] model.name      = {}", name);
        log.info("[NICE][SUCCESS] model.birth     = {}", birth);
        log.info("[NICE][SUCCESS] model.mobileNo  = {}", mobileNo);
        log.info("[ADMIN_LOGIN] ENTER handleSuccess purpose={}, sessionId={}", purpose, session.getId());

        if (purpose == null) {
            log.error("[CHECKPLUS] purpose가 null입니다. 세션이 유실되었을 수 있습니다. sessionId={}", session.getId());
            model.addAttribute("errorMessage", "본인인증 세션이 만료되었습니다. 다시 시도해 주세요.");
            return "common/modal/checkplus_error";
        }

        // 화면값에 따른 분기
        switch (purpose) {
            case "SIGNUP":
                // 전화번호 파싱
                String mpnoPfx = mobileNo.substring(0, 3);
                String mpnoMid = mobileNo.substring(3, 7);
                String mpnoSfx = mobileNo.substring(7);

                // 중복 가입 체크 (이름 + 생년월일 + 전화번호)
                EgovMap identityMap = new EgovMap();
                identityMap.put("userNmKorn", name);
                identityMap.put("brdt", birth);
                identityMap.put("mpnoPfx", mpnoPfx);
                identityMap.put("mpnoMid", mpnoMid);  // 평문으로 전달
                identityMap.put("mpnoSfx", mpnoSfx);
                identityMap.put("aesKey", aesComponent.getSecretKey());  // AES 키 추가

                int existingUserCount = userMapper.countUserByIdentity(identityMap);

                if (existingUserCount > 0) {
                    log.warn("[SIGNUP] 이미 가입된 회원 정보: name={}, birth={}, mobile={}", name, birth, mobileNo);
                    model.addAttribute("errorMessage", "이미 가입된 회원 정보입니다.");
                    model.addAttribute("purpose", "SIGNUP");
                    return "common/modal/checkplus_error";
                }

                // 회원가입 성공 처리
                session.setAttribute("signup_phone_verified", true);
                model.addAttribute("name",     name);
                model.addAttribute("birth",    birth);
                model.addAttribute("mobileNo", mobileNo);

                return "common/modal/checkplus_success_3";

            case "RESET_PWD":
                // 리셋용 사용자 아이디 세션에서 가져오기
                String resetUserId = (String) session.getAttribute("PWD_RESET_USER_ID");

                if (resetUserId == null) {
                    model.addAttribute("errorMessage", "비밀번호 재설정 대상 아이디가 없습니다.");
                    return "common/modal/checkplus_error";
                }

                NicePassVerifyCallbackDto callbackDto = new NicePassVerifyCallbackDto();
                callbackDto.setUserId(resetUserId);
                callbackDto.setName(name);
                callbackDto.setBrdt(birth);

                callbackDto.setMpnoPfx(mobileNo.substring(0, 3));
                callbackDto.setMpnoMid(mobileNo.substring(3, 7));
                callbackDto.setMpnoSfx(mobileNo.substring(7));

                // DB 조회
                Long userOid = authService.getUserByNiceCallbackDto(callbackDto);
                log.info("[NICE][SUCCESS] userOid     = {}", userOid);

                if (userOid == null) {
                    model.addAttribute("errorMessage", "아이디 또는 본인인증 정보가 회원정보와 일치하지 않습니다.");
                    return "common/modal/checkplus_error";
                }

                // 인증 성공
                session.setAttribute("PWD_RESET_VERIFIED", "Y");
                session.setAttribute("PWD_RESET_USER_OID", userOid);

                return "common/modal/checkplus_success_reset_pwd";

            case "ADMIN_RESET_PWD":
                // 관리자 비밀번호 재설정용 사용자 아이디 세션에서 가져오기
                String adminResetUserId = (String) session.getAttribute("ADMIN_PWD_RESET_USER_ID");

                if (adminResetUserId == null) {
                    model.addAttribute("errorMessage", "비밀번호 재설정 대상 아이디가 없습니다.");
                    return "common/modal/checkplus_error";
                }

                NicePassVerifyCallbackDto adminCallbackDto = new NicePassVerifyCallbackDto();
                adminCallbackDto.setUserId(adminResetUserId);
                adminCallbackDto.setName(name);
                adminCallbackDto.setBrdt(birth);

                adminCallbackDto.setMpnoPfx(mobileNo.substring(0, 3));
                adminCallbackDto.setMpnoMid(mobileNo.substring(3, 7));
                adminCallbackDto.setMpnoSfx(mobileNo.substring(7));

                // DB 조회
                Long adminUserNo = authService.getUserByNiceCallbackDto(adminCallbackDto);
                log.info("[NICE][SUCCESS] adminUserNo = {}", adminUserNo);

                if (adminUserNo == null) {
                    model.addAttribute("errorMessage", "아이디 또는 본인인증 정보가 회원정보와 일치하지 않습니다.");
                    return "common/modal/checkplus_error";
                }

                // 인증 성공
                session.setAttribute("ADMIN_PWD_RESET_VERIFIED", "Y");
                session.setAttribute("ADMIN_PWD_RESET_USER_NO", adminUserNo);

                return "common/modal/checkplus_success_admin_reset_pwd";

            case "ADMIN_LOGIN":
                // 세션에 저장해둔 인증된 관리자 정보 꺼내기
                String adminUserId = (String) session.getAttribute("SECOND_AUTH_ADMIN_USER_ID");
                String adminName   = (String) session.getAttribute("SECOND_AUTH_NAME");
                String adminBrdt   = (String) session.getAttribute("SECOND_AUTH_BRDT");
                String adminMpPfx  = (String) session.getAttribute("SECOND_AUTH_MPNO_PFX");
                String adminMpMid  = (String) session.getAttribute("SECOND_AUTH_MPNO_MID");
                String adminMpSfx  = (String) session.getAttribute("SECOND_AUTH_MPNO_SFX");

                if (adminUserId == null) {
                    log.error("[NICE][ADMIN_LOGIN] 본인인증 실패 - 세션에 관리자 정보 없음");

                    // 실패 로그 기록
                    String loginSessionId = (String) session.getAttribute("SECOND_AUTH_LOGIN_SESSION_ID");
                    if (loginSessionId == null) {
                        loginSessionId = jwtTokenProvider.generateLoginSessionId();
                    }

                    auditService.logLoginAttempt(
                            request,
                            "UNKNOWN",
                            null,
                            LoginResult.FAIL,
                            "2차 본인인증 - 세션에 관리자 정보 없음",
                            loginSessionId
                    );

                    model.addAttribute("errorMessage", "관리자 로그인 정보가 존재하지 않습니다.");
                    model.addAttribute("purpose", "ADMIN_LOGIN");
                    return "common/modal/checkplus_error";
                }

                // 전화번호 포맷 맞추기
                String savedMobile = adminMpPfx + adminMpMid + adminMpSfx;
                String normalizedNiceMobile = mobileNo.replaceAll("[^0-9]", ""); // 혹시라도 '-' 들어오면 제거

                boolean sameName  = adminName.equals(name);
                boolean sameBirth = adminBrdt.equals(birth);
                boolean samePhone = savedMobile.equals(normalizedNiceMobile);

                log.info("[NICE][ADMIN_LOGIN] compare => name:{}/{} birth:{}/{} phone:{}/{}",
                        adminName, name, adminBrdt, birth, savedMobile, normalizedNiceMobile);

                if (!(sameName && sameBirth && samePhone)) {
                    log.error("[NICE][ADMIN_LOGIN] 본인인증 실패 - 정보 불일치");

                    // 실패 로그 기록
                    String loginSessionId = (String) session.getAttribute("SECOND_AUTH_LOGIN_SESSION_ID");
                    if (loginSessionId == null) {
                        loginSessionId = jwtTokenProvider.generateLoginSessionId();
                    }

                    auditService.logLoginAttempt(
                            request,
                            adminUserId,
                            null,
                            LoginResult.FAIL,
                            "2차 본인인증 정보 불일치 (이름:" + sameName + ", 생년월일:" + sameBirth + ", 전화번호:" + samePhone + ")",
                            loginSessionId
                    );

                    model.addAttribute("errorMessage", "관리자 계정 정보와 본인인증 정보가 일치하지 않습니다.");
                    model.addAttribute("purpose", "ADMIN_LOGIN");
                    return "common/modal/checkplus_error";
                }

                BaseUser adminUser = authService.getActiveUserById(adminUserId);
                log.info("[ADMIN_LOGIN] sessionId={}, adminUserId={}",
                        session.getId(),
                        adminUserId);

                if (adminUser == null) {
                    log.error("[NICE][ADMIN_LOGIN] 본인인증 실패 - 관리자 계정을 찾을 수 없음: {}", adminUserId);

                    // 실패 로그 기록
                    String loginSessionId = (String) session.getAttribute("SECOND_AUTH_LOGIN_SESSION_ID");
                    if (loginSessionId == null) {
                        loginSessionId = jwtTokenProvider.generateLoginSessionId();
                    }

                    auditService.logLoginAttempt(
                            request,
                            adminUserId,
                            null,
                            LoginResult.FAIL,
                            "2차 본인인증 - 관리자 계정을 찾을 수 없음",
                            loginSessionId
                    );

                    model.addAttribute("errorMessage", "관리자 계정을 찾을 수 없습니다.");
                    model.addAttribute("purpose", "ADMIN_LOGIN");
                    return "common/modal/checkplus_error";
                }

                // 기존 세션 무효화 (단일 로그인 정책)
                if (sessionService.hasExistingSession(adminUserId)) {
                    log.info("Admin login: invalidating existing session for user: {}", adminUserId);
                    sessionService.invalidateSessionByUserId(adminUserId);
                }

                // 토큰 발급 + 쿠키 세팅 + 감사 로그
                String loginSessionId = (String) session.getAttribute("SECOND_AUTH_LOGIN_SESSION_ID");
                authService.issueTokensAndCreateSession(adminUser, request, response, loginSessionId);

                // 세션 정리
                session.removeAttribute("SECOND_AUTH_ADMIN_USER_ID");
                session.removeAttribute("SECOND_AUTH_NAME");
                session.removeAttribute("SECOND_AUTH_BRDT");
                session.removeAttribute("SECOND_AUTH_MPNO_PFX");
                session.removeAttribute("SECOND_AUTH_MPNO_MID");
                session.removeAttribute("SECOND_AUTH_MPNO_SFX");

                // 팝업에서 사용할 값들 모델에 넣기
                model.addAttribute("resultMessage", "관리자 본인인증이 완료되었습니다.");

                return "common/modal/checkplus_success_admin_login";

            case "CHANGE_PWD":
                // 비밀번호 변경을 위한 본인인증
                log.info("[NICE][CHANGE_PWD] 본인인증 완료 - name={}, birth={}, mobile={}", name, birth, mobileNo);

                // 세션에 본인인증 정보 저장 (프론트에서 사용)
                session.setAttribute("CHANGE_PWD_VERIFIED", "Y");
                session.setAttribute("CHANGE_PWD_NAME", name);
                session.setAttribute("CHANGE_PWD_BIRTH", birth);
                session.setAttribute("CHANGE_PWD_MOBILE_NO", mobileNo);

                // 모델에 데이터 추가하여 팝업에 전달
                model.addAttribute("name", name);
                model.addAttribute("birth", birth);
                model.addAttribute("mobileNo", mobileNo);
                
                return "common/modal/checkplus_success_change_pwd";

            default:
                model.addAttribute("errorMessage", "지원하지 않는 인증 목적입니다.");
                return "common/modal/checkplus_error";
        }
    }
}