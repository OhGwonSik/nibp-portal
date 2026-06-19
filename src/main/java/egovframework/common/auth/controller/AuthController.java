package egovframework.common.auth.controller;

import egovframework.common.api.ApiResponse;
import egovframework.common.audit.dto.UpdateLogoutInfoDto;
import egovframework.common.audit.enums.LoginResult;
import egovframework.common.audit.service.AuditService;
import egovframework.common.auth.domain.*;
import egovframework.common.auth.service.AuthService;
import egovframework.common.auth.service.RefreshTokenService;
import egovframework.common.auth.service.SessionService;
import egovframework.common.enums.AuthLevel;
import egovframework.common.jwt.JwtTokenProvider;
import egovframework.common.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuditService auditService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;
    private final CookieUtil cookieUtil;
    private final egovframework.common.email.service.EmailService emailService;

    @Value("${common.base-url:http://localhost:8080}")
    private String baseUrl;

    @PostMapping("/login")
    public ResponseEntity<?> userLogin(
            @RequestBody LoginRequestDto loginRequest,
            @RequestParam(value = "force", defaultValue = "false") boolean force,
            HttpServletRequest request, HttpServletResponse response) {
        log.info("Login attempt for user: {}, force: {}", loginRequest.getUserId(), force);

        try {
            BaseUser authenticatedUser = authService.authenticate(loginRequest.getUserId(), loginRequest.getPassword());

            // 관리자 계정이면 바로 에러 리턴
            if ("ADMIN".equals(authenticatedUser.getUserAuthrt())) {
                String fileExpln = "관리자 계정입니다. 관리자 페이지에서 로그인해주세요.";

                // 로그인 실패 로그 기록 (관리자 계정으로 포털 로그인 시도)
                auditService.logLoginAttempt(
                        request,
                        loginRequest.getUserId(),
                        authenticatedUser,
                        LoginResult.FAIL,
                        "관리자 계정으로 포털 로그인 시도",
                        null
                );
                return new ResponseEntity<>(ApiResponse.error(HttpStatus.FORBIDDEN, fileExpln), HttpStatus.FORBIDDEN);
            }

            String userId = authenticatedUser.getUserId();

            // Redis에서 기존 세션 확인
            Optional<UserSession> existingSession = sessionService.getSession(userId);
            if (existingSession.isPresent() && !force) {
                // 기존 세션 있고 force가 아니면 확인 요청
                UserSession session = existingSession.get();
                String loginAtStr = session.getLoginAt() != null
                        ? session.getLoginAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : "알 수 없음";

                log.info("Existing session found for user: {}, loginAt: {}", userId, loginAtStr);

                Map<String, Object> sessionInfo = new HashMap<>();
                sessionInfo.put("sessionExists", true);
                sessionInfo.put("loginAt", loginAtStr);

                return new ResponseEntity<>(
                        ApiResponse.of(HttpStatus.CONFLICT.value(), "이미 로그인 중입니다. 계속하면 기존 세션이 로그아웃됩니다.", sessionInfo),
                        HttpStatus.CONFLICT
                );
            }

            // 기존 세션 있고 force=true면 기존 세션 무효화 (새 세션이 덮어쓰므로 생략 가능하나 명시적으로 삭제)
            if (existingSession.isPresent() && force) {
                log.info("Force login: invalidating existing session for user: {}", userId);

                // 기존 세션의 로그아웃 시간 기록
                UserSession oldSession = existingSession.get();
                if (oldSession.getLoginSessionId() != null) {
                    UpdateLogoutInfoDto dto = UpdateLogoutInfoDto.builder()
                            .userId(userId)
                            .userOid(authenticatedUser.getUserOid())
                            .loginSsnId(oldSession.getLoginSessionId())
                            .build();
                    auditService.updateLogoutInfo(dto);
                    log.info("Force login: updated logout info for old session. userId={}, loginSessionId={}",
                            userId, oldSession.getLoginSessionId());
                }

                sessionService.invalidateSession(userId);
            }

            LoginResponseDto loginResponseDto = authService.issueTokensAndCreateSession(authenticatedUser, request, response, null);

            log.info("Login successful for user: {}", authenticatedUser.getUserId());
            return new ResponseEntity<>(ApiResponse.success(loginResponseDto), HttpStatus.OK);

        } catch (BadCredentialsException e) {
            String fileExpln = e.getMessage() != null ? e.getMessage() : "";
            log.warn("Login bad credentials for user: {} => {}", loginRequest.getUserId(), fileExpln);

            // 잠김 문구 포함 여부 체크
            boolean locked = fileExpln.contains("로그인 실패 횟수를 초과하여 계정이 잠겼습니다");

            // 로그인 실패 로그 기록
            auditService.logLoginAttempt(request, loginRequest.getUserId(), null, LoginResult.FAIL, fileExpln, null);

            // 잠김이면 423 LOCKED, 아니면 401 UNAUTHORIZED
            HttpStatus stts = locked ? HttpStatus.LOCKED : HttpStatus.UNAUTHORIZED;
            return new ResponseEntity<>(ApiResponse.error(stts, fileExpln), stts);

        } catch (Exception e) {
            log.error("Login error for user: {}", loginRequest.getUserId(), e);
            // 로그인 실패 로그 기록
            auditService.logLoginAttempt(request, loginRequest.getUserId(), null, LoginResult.FAIL, e.getMessage(), null);
            throw e;
            // return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "로그인 처리 중 오류가 발생했습니다."), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequestDto loginRequest,
                                        @RequestParam(value = "force", defaultValue = "false") boolean force,
                                        HttpServletRequest request, HttpServletResponse response) {
        log.info("Login attempt for admin user: {}, force: {}", loginRequest.getUserId(), force);
        LoginResponseDto loginResponseDto = null;
        try {
            BaseUser authenticatedUser = authService.authenticate(loginRequest.getUserId(), loginRequest.getPassword());

            // 관리자 계정이 아니면 바로 에러 리턴
            if (!"ADMIN".equals(authenticatedUser.getUserAuthrt())) {
                String fileExpln = "관리자 계정만 로그인할 수 있습니다.";

                // 로그인 실패 로그 기록 (관리자 계정으로 포털 로그인 시도)
                auditService.logLoginAttempt(
                        request,
                        loginRequest.getUserId(),
                        authenticatedUser,
                        LoginResult.FAIL,
                        "비관리자 계정의 관리자페이지 로그인 시도",
                        null
                );
                return new ResponseEntity<>(ApiResponse.error(HttpStatus.FORBIDDEN, fileExpln), HttpStatus.FORBIDDEN);
            }

            String userId = authenticatedUser.getUserId();

            // Redis에서 기존 세션 확인
            Optional<UserSession> existingSession = sessionService.getSession(userId);
            if (existingSession.isPresent() && !force) {
                // 기존 세션 있고 force가 아니면 확인 요청
                UserSession session = existingSession.get();
                String loginAtStr = session.getLoginAt() != null
                        ? session.getLoginAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : "알 수 없음";

                log.info("Existing admin session found for user: {}, loginAt: {}", userId, loginAtStr);

                Map<String, Object> sessionInfo = new HashMap<>();
                sessionInfo.put("sessionExists", true);
                sessionInfo.put("loginAt", loginAtStr);

                return new ResponseEntity<>(
                        ApiResponse.of(HttpStatus.CONFLICT.value(), "이미 로그인 중입니다. 계속하면 기존 세션이 로그아웃됩니다.", sessionInfo),
                        HttpStatus.CONFLICT
                );
            }

            // 기존 세션 있고 force=true면 기존 세션 무효화 (새 세션이 덮어쓰므로 생략 가능하나 명시적으로 삭제)
            if (existingSession.isPresent() && force) {
                log.info("Force admin login: invalidating existing session for user: {}", userId);

                // 기존 세션의 로그아웃 시간 기록
                UserSession oldSession = existingSession.get();
                if (oldSession.getLoginSessionId() != null) {
                    UpdateLogoutInfoDto dto = UpdateLogoutInfoDto.builder()
                            .userId(userId)
                            .userOid(authenticatedUser.getUserOid())
                            .loginSsnId(oldSession.getLoginSessionId())
                            .build();
                    auditService.updateLogoutInfo(dto);
                    log.info("Force admin login: updated logout info for old session. userId={}, loginSessionId={}",
                            userId, oldSession.getLoginSessionId());
                }

                sessionService.invalidateSession(userId);
            }

            /* ===== 2차인증 비활성화 (주석 처리) — 복원 시 아래 블록 주석 해제 =====
            // 1) 2차 인증 식별용 토큰 생성
            String secondAuthToken = jwtTokenProvider.generateLoginSessionId();

            // 2) 서버 쪽에 이 토큰으로 현재 로그인 시도 컨텍스트를 저장
            HttpSession session = request.getSession();
            session.setAttribute("SECOND_AUTH:" + secondAuthToken, authenticatedUser.getUserId());
            session.setAttribute("SECOND_AUTH_NAME", authenticatedUser.getUserNmKorn());
            session.setAttribute("SECOND_AUTH_BRDT", authenticatedUser.getBrdt());
            session.setAttribute("SECOND_AUTH_MPNO_PFX", authenticatedUser.getMpnoPfx());
            session.setAttribute("SECOND_AUTH_MPNO_MID", authenticatedUser.getMpnoMid());
            session.setAttribute("SECOND_AUTH_MPNO_SFX", authenticatedUser.getMpnoSfx());
            session.setAttribute("SECOND_AUTH_ADMIN_USER_ID", authenticatedUser.getUserId());

            // 3) 팝업에서 열 주소 생성
            String popupUrl = request.getContextPath() + "/api/common/portal/checkplus/main?purpose=ADMIN_LOGIN&token=" + secondAuthToken;

            // 4) 프론트에 내려줄 응답 DTO 구성
            BaseUserDto userDto = BaseUserDto.fromUser(authenticatedUser);

            loginResponseDto = LoginResponseDto.builder()
                    .user(userDto)
                    .secondAuthRequired(true)
                    .popupUrl(popupUrl)
                    .build();

            log.info("Admin user requires second auth. userId={}, secondAuthToken={}",
                    authenticatedUser.getUserId(), secondAuthToken);
            ===== 2차인증 비활성화 끝 ===== */

            // 2차인증 없이 바로 토큰 발급
            loginResponseDto = authService.issueTokensAndCreateSession(authenticatedUser, request, response, null);

            log.info("Admin login successful for user: {}", authenticatedUser.getUserId());

        } catch (BadCredentialsException e) {
            String fileExpln = e.getMessage() != null ? e.getMessage() : "";
            log.warn("Admin login bad credentials for user: {} => {}", loginRequest.getUserId(), fileExpln);

            boolean locked = fileExpln.contains("로그인 실패 횟수를 초과하여 계정이 잠겼습니다");

            auditService.logLoginAttempt(request, loginRequest.getUserId(), null, LoginResult.FAIL, fileExpln, null);

            HttpStatus stts = locked ? HttpStatus.LOCKED : HttpStatus.UNAUTHORIZED;
            return new ResponseEntity<>(ApiResponse.error(stts, fileExpln), stts);

        } catch (Exception e) {
            log.error("Login error for user: {}", loginRequest.getUserId(), e);
            auditService.logLoginAttempt(request, loginRequest.getUserId(), null, LoginResult.FAIL, e.getMessage(), null);
            throw e;
        }
        return new ResponseEntity<>(ApiResponse.success(loginResponseDto), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        log.info("Logout request");
        try {
            String token = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            } else {
                token = CookieUtil.getAccessToken(request);
            }

            if (token != null) {
                try {
                    String userId = jwtTokenProvider.getUserId(token);
                    String loginSsnId = jwtTokenProvider.getLoginSessionId(token);
                    BaseUser user = authService.getUserById(userId);

                    // 로그아웃 감사 로그 기록
                    if (user != null && loginSsnId != null) {
                        UpdateLogoutInfoDto dto = UpdateLogoutInfoDto.builder()
                            .userId(userId)
                            .userOid(user.getUserOid())
                            .loginSsnId(loginSsnId)
                            .build();
                        auditService.updateLogoutInfo(dto);
                    }

                    // Redis 세션 무효화
                    sessionService.invalidateSessionByUserId(userId);
                    log.info("Redis session invalidated for user: {}", userId);

                } catch (Exception e) {
                    log.warn("Failed to process logout for token: {}", e.getMessage());
                }
            }

            // 쿠키 삭제
            cookieUtil.deleteAuthCookies(response);
            log.debug("Auth cookies deleted");
            log.info("Logout successful");
            return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "로그아웃 처리 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponseDto>> refresh(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody(required = false) Map<String, String> requestBody) {
        log.info("Token refresh request");
        String refreshToken = null;
        if (requestBody != null && requestBody.containsKey("refreshToken")) {
            refreshToken = requestBody.get("refreshToken");
        } else {
            refreshToken = CookieUtil.getRefreshToken(request);
        }

        if (refreshToken == null || refreshToken.isEmpty()) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.BAD_REQUEST, "Refresh Token이 필요합니다."), HttpStatus.BAD_REQUEST);
        }

        try {
            // JWT 유효성 검증
            if (!refreshTokenService.validateRefreshToken(refreshToken)) {
                cookieUtil.deleteAuthCookies(response);
                return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."), HttpStatus.UNAUTHORIZED);
            }

            String userId = jwtTokenProvider.getUserId(refreshToken);
            String refreshTokenId = jwtTokenProvider.getTokenId(refreshToken);
            String loginSsnId = jwtTokenProvider.getLoginSessionId(refreshToken);  // loginSessionId 추출 (유지용)

            // 세션의 refreshTokenId와 일치하는지 확인
            if (!sessionService.validateRefreshToken(userId, refreshTokenId)) {
                log.warn("Refresh Token이 세션과 불일치. 다른 곳에서 로그인됨. userId={}", userId);
                cookieUtil.deleteAuthCookies(response);
                return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "세션이 만료되었습니다. 다시 로그인해주세요."), HttpStatus.UNAUTHORIZED);
            }
            BaseUser user = authService.getActiveUserById(userId);

            if (user == null) {
                return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."), HttpStatus.UNAUTHORIZED);
            }

            // AccessLogInterceptor의 ApiAccessLog 객체를 직접 수정
            try {
                egovframework.common.audit.domain.ApiAccessLog apiAccessLog =
                        (egovframework.common.audit.domain.ApiAccessLog) request.getAttribute("apiAccessLog");
                if (apiAccessLog != null) {
                    String oldLoginSsnId = apiAccessLog.getSsnId();
                    apiAccessLog.setSsnId(loginSsnId);
                    log.info("[LoginSsnId] REFRESH - Updated ApiAccessLog directly: {} -> {}", oldLoginSsnId, loginSsnId);
                }
            } catch (Exception e) {
                log.error("[LoginSsnId] REFRESH - Failed to update ApiAccessLog: {}", e.getMessage());
            }

            String userAuthrt = user.getUserAuthrt();
            long accessTokenExp = jwtTokenProvider.getAccessTokenExpiration(userAuthrt);
            long refreshTokenExp = jwtTokenProvider.getRefreshTokenExpiration(userAuthrt);

            String newAccessToken = jwtTokenProvider.generateAccessToken(user, loginSsnId);  // loginSessionId 포함
            String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);

            if (newRefreshToken == null) {
                return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "토큰 갱신 실패"), HttpStatus.UNAUTHORIZED);
            }

            // Redis 세션 업데이트 (새 토큰 정보로)
            String newRefreshTokenId = jwtTokenProvider.getTokenId(newRefreshToken);
            sessionService.updateSession(userId, userAuthrt, newAccessToken, newRefreshTokenId);

            response.addHeader("Set-Cookie", cookieUtil.createAccessTokenCookie(newAccessToken, (int) (accessTokenExp / 1000)));
            response.addHeader("Set-Cookie", cookieUtil.createRefreshTokenCookie(newRefreshToken, (int) (refreshTokenExp / 1000)));

            BaseUserDto userDto = BaseUserDto.fromUser(user);

            LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                    .user(userDto)
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(accessTokenExp / 1000)
                    .build();

            log.info("Token refresh successful for user: {}", userId);
            return new ResponseEntity<>(ApiResponse.success(loginResponseDto), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Token refresh error", e);
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "토큰 갱신 중 오류가 발생했습니다."), HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/admin/me")
    public ResponseEntity<ApiResponse<BaseUserDto>> updateAdminMe(@Valid @RequestBody AdminUserUpdateDto adminUserUpdateDto, @AuthenticationPrincipal BaseUser principal) {
        if (principal == null) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."), HttpStatus.UNAUTHORIZED);
        }
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.FORBIDDEN, "관리자만 접근 가능합니다."), HttpStatus.FORBIDDEN);
        }
        try {
            BaseUserDto userDto = authService.updateAdminMe(adminUserUpdateDto, principal);
            return new ResponseEntity<>(ApiResponse.success(userDto), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Update current admin user error", e);
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "정보 수정 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // update 및 재조회를 하여 post
    @PostMapping("/admin/me/updatePswd")
    public ResponseEntity<ApiResponse<Integer>> updateAdminMePswd(@Valid @RequestBody AdminUserUpdatePwdDto adminUserUpdatePwdDto,
                                                                  @AuthenticationPrincipal BaseUser principal,
                                                                  HttpServletResponse response) {
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "관리자만 접근 가능합니다."), HttpStatus.UNAUTHORIZED);
        }

        try {
            Integer result = authService.updateAdminMePswd(adminUserUpdatePwdDto);

            // 비밀번호 변경 후 세션 무효화 (보안: 이전 토큰 무효화)
            sessionService.invalidateSession(principal.getUserId());
            cookieUtil.deleteAuthCookies(response);
            log.info("비밀번호 변경 후 세션 무효화: userId={}", principal.getUserId());

            return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Update current user password error", e);
            throw e;
        }
    }

    @PostMapping("/admin/join")
    public ResponseEntity<ApiResponse<Void>> insertJoinAdminUser(@RequestBody @Valid BaseUser user, @AuthenticationPrincipal BaseUser principal) {
        try {
            if (principal == null || !"ADMIN".equals(principal.getUserAuthrt())) {
                return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "관리자만 접근 가능합니다."), HttpStatus.UNAUTHORIZED);
            }
            authService.insertJoinAdminUser(user);
            return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Insert join admin user error", e);
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.BAD_REQUEST, "관리자 생성 중 오류가 발생했습니다."), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin/checkId")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkUserId(@Valid @RequestBody CheckIdRequestDto checkIdRequestDto,
                                                                        @AuthenticationPrincipal BaseUser principal) {
        if (principal == null || !"ADMIN".equals(principal.getUserAuthrt())) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "관리자만 접근 가능합니다."), HttpStatus.UNAUTHORIZED);
        }
        int cnt = authService.checkAdminId(checkIdRequestDto);

        Map<String, Object> result = new HashMap<>();
        result.put("duplicated", cnt > 0);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/admin/checkEmail")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@Valid @RequestBody CheckEmailRequestDto checkEmailRequestDto,
                                                           @AuthenticationPrincipal BaseUser principal) {
        try {
            if (principal == null || !AuthLevel.ADMIN.name().equals(principal.getUserAuthrt())) {
                return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "관리자만 접근 가능합니다."), HttpStatus.UNAUTHORIZED);
            }
            boolean result = authService.checkEmail(checkEmailRequestDto);
            return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Check email error", e);
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.BAD_REQUEST, "이메일 중복 체크 중 오류가 발생했습니다."), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin/checkPhone")
    public ResponseEntity<ApiResponse<Boolean>> checkPhone(@Valid @RequestBody CheckPhoneRequestDto checkPhoneRequestDto,
                                                           @AuthenticationPrincipal BaseUser principal) {
        try {
            if (principal == null || !"ADMIN".equals(principal.getUserAuthrt())) {
                return new ResponseEntity<>(ApiResponse.error(HttpStatus.UNAUTHORIZED, "관리자만 접근 가능합니다."), HttpStatus.UNAUTHORIZED);
            }
            boolean result = authService.checkPhone(checkPhoneRequestDto);
            return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Check phone error", e);
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.BAD_REQUEST, "휴대폰 번호 중복 체크 중 오류가 발생했습니다."), HttpStatus.BAD_REQUEST);
        }
    }

    // 관리자 아이디 찾기
    @PostMapping("/admin/find/id")
    public ResponseEntity<ApiResponse<String>> findAdminId(@Valid @RequestBody FindIdRequestDto requestDto) {
        log.info("Admin find ID request for user: {}", requestDto.getUserName());
        try {
            String userId = authService.findAdminId(requestDto);
            return new ResponseEntity<>(ApiResponse.success(userId), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Admin find ID error", e);
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.BAD_REQUEST, "아이디 찾기 중 오류가 발생했습니다."), HttpStatus.BAD_REQUEST);
        }
    }

    // 관리자 비밀번호 찾기 - 임시 비밀번호 발급 및 이메일 발송
    @PostMapping("/admin/find/password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> findAdminPassword(@Valid @RequestBody FindPasswordRequestDto requestDto,
                                                                               HttpSession session) {
        log.info("Admin find password request for user: {}", requestDto.getUserId());

        /* ===== 2차인증 비활성화 (주석 처리) — 복원 시 아래 블록 주석 해제 =====
        String verified = (String) session.getAttribute("ADMIN_PWD_RESET_VERIFIED");
        Long userOid = (Long) session.getAttribute("ADMIN_PWD_RESET_USER_NO");

        if (!"Y".equals(verified) || userOid == null) {
            Map<String, Object> failResult = new HashMap<>();
            failResult.put("found", false);
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.UNAUTHORIZED, "본인인증이 필요합니다."),
                    HttpStatus.UNAUTHORIZED
            );
        }

        session.removeAttribute("ADMIN_PWD_RESET_VERIFIED");
        session.removeAttribute("ADMIN_PWD_RESET_USER_NO");
        ===== 2차인증 비활성화 끝 ===== */

        try {
            // 1. 관리자 정보 확인 및 임시 비밀번호 발급
            String tempPassword = authService.findAdminPasswordAndSendEmail(requestDto);

            // 2. 관리자 정보 조회 (이메일 발송용)
            BaseUser user = authService.getUserById(requestDto.getUserId());

            if (user == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("found", false);
                return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
            }

            // 3. 이메일 발송
            String toEmail = user.getEmlLcal() + "@" + user.getEmlDmn();

            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", user.getUserNmKorn());
            variables.put("tempPassword", tempPassword);
            variables.put("loginUrl", baseUrl + "/admin/login");

            egovframework.common.email.dto.EmailMessage emailMessage = egovframework.common.email.dto.EmailMessage.builder()
                    .to(new String[]{toEmail})
                    .subject("[국가생명윤리정책원] 관리자 임시 비밀번호 발급 안내")
                    .build();

            emailService.sendEmailWithTemplate(emailMessage, "email/temp_password", variables);

            log.info("Admin temporary password email sent to: {}", toEmail);

            Map<String, Object> result = new HashMap<>();
            result.put("found", true);
            return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Admin find password error", e);
            Map<String, Object> result = new HashMap<>();
            result.put("found", false);
            return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
        }
    }

    // 관리자 비밀번호 재설정 > 비밀번호 업데이트
    @PostMapping("/admin/updatePswd")
    public ResponseEntity<ApiResponse<Integer>> updateAdminPwd(@Valid @RequestBody PasswordResetConfirmDto dto,
                                                               HttpSession session,
                                                               HttpServletResponse response) {
        try {
            // 세션에서 본인인증 플래그 / 유저번호 / 유저ID 꺼내기
            String verified = (String) session.getAttribute("ADMIN_PWD_RESET_VERIFIED");
            Long userOid = (Long) session.getAttribute("ADMIN_PWD_RESET_USER_NO");
            String userId = (String) session.getAttribute("ADMIN_PWD_RESET_USER_ID");

            if (!"Y".equals(verified) || userOid == null) {
                return new ResponseEntity<>(
                        ApiResponse.error(HttpStatus.UNAUTHORIZED, "본인인증이 필요합니다."),
                        HttpStatus.UNAUTHORIZED
                );
            }

            dto.setUserOid(userOid);
            Integer result = authService.resetPassword(dto);

            // 비밀번호 변경 후 해당 사용자의 세션 무효화
            if (userId != null) {
                sessionService.invalidateSession(userId);
                log.info("관리자 비밀번호 재설정 후 세션 무효화: userId={}", userId);
            }

            // 세션 값 제거
            session.removeAttribute("ADMIN_PWD_RESET_VERIFIED");
            session.removeAttribute("ADMIN_PWD_RESET_USER_NO");
            session.removeAttribute("ADMIN_PWD_RESET_USER_ID");

            return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Update admin password error", e);
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // 비밀번호 재설정 > 비밀번호 업데이트
    @PostMapping("/common/updatePswd")
    public ResponseEntity<ApiResponse<Integer>> updatePswd(@Valid @RequestBody PasswordResetConfirmDto dto,
                                                          HttpSession session,
                                                          HttpServletResponse response) {
        try {
            // 1) 세션에서 본인인증 플래그 / 유저번호 / 유저ID 꺼내기
            String verified = (String) session.getAttribute("PWD_RESET_VERIFIED");
            Long userOid = (Long) session.getAttribute("PWD_RESET_USER_OID");
            String userId = (String) session.getAttribute("PWD_RESET_USER_ID");

            if (!"Y".equals(verified) || userOid == null) {
                // 본인인증 안 되어있으면 비번 변경 못 하게
                return new ResponseEntity<>(
                        ApiResponse.error(HttpStatus.UNAUTHORIZED, "본인인증이 필요합니다."),
                        HttpStatus.UNAUTHORIZED
                );
            }

            dto.setUserOid(userOid);
            Integer result = authService.resetPassword(dto);

            // 2) 비밀번호 변경 후 해당 사용자의 세션 무효화 (보안: 이전 토큰 무효화)
            if (userId != null) {
                sessionService.invalidateSession(userId);
                log.info("비밀번호 재설정 후 세션 무효화: userId={}", userId);
            }

            // 3) 한 번 쓰고 세션 값 제거 (보안상 깔끔)
            session.removeAttribute("PWD_RESET_VERIFIED");
            session.removeAttribute("PWD_RESET_USER_OID");
            session.removeAttribute("PWD_RESET_USER_ID");

            return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Update current user password error", e);
            return new ResponseEntity<>(
                    ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

}