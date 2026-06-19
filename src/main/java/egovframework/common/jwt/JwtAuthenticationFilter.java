package egovframework.common.jwt;

import egovframework.common.auth.domain.BaseUser;
import egovframework.common.auth.service.AuthService;
import egovframework.common.auth.service.CustomUserDetailsService;
import egovframework.common.auth.service.RefreshTokenService;
import egovframework.common.auth.service.SessionService;
import egovframework.common.security.SecurityConstants;
import egovframework.common.util.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;
    private final CookieUtil cookieUtil;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(
            CustomUserDetailsService customUserDetailsService,
            JwtTokenProvider jwtTokenProvider,
            AuthService authService,
            RefreshTokenService refreshTokenService,
            SessionService sessionService,
            CookieUtil cookieUtil) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.sessionService = sessionService;
        this.cookieUtil = cookieUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String rqtUri = request.getRequestURI();

        // 리소스 요청 제외 (CSS, JS, 이미지 등)
        if (isResourceRequest(rqtUri)) {
            chain.doFilter(request, response);
            return;
        }

        String username = null;
        String userAuthrt = null;
        String jwtToken = null; // This will hold the access token

        // 1. Authorization 헤더에서 토큰 추출 시도
        final String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            log.debug("JWT Token extracted from Authorization header");
        }

        // 2. 헤더에 토큰이 없으면 쿠키에서 추출 시도
        if (jwtToken == null) {
            jwtToken = CookieUtil.getAccessToken(request);
            if (jwtToken != null) {
                log.debug("JWT Token extracted from cookie");
            }
        }

        // 3. 토큰에서 사용자 ID 추출 및 토큰 갱신 시도
        if (jwtToken != null) {
            try {
                username = jwtTokenProvider.getUserId(jwtToken);
                userAuthrt = jwtTokenProvider.getClaimValue(jwtToken, "userAuthrt");
            } catch (ExpiredJwtException e) {
                log.warn("Access Token has expired. Attempting to refresh token.");
                String newAccessToken = attemptTokenRefresh(request, response);
                if (newAccessToken != null) {
                    jwtToken = newAccessToken;
                    try {
                        username = jwtTokenProvider.getUserId(jwtToken);
                        userAuthrt = jwtTokenProvider.getClaimValue(jwtToken, "userAuthrt");
                    } catch (Exception ex) {
                        log.error("Failed to extract user info from newly refreshed access token: {}", ex.getMessage());
                        jwtToken = null;
                    }
                } else {
                    // 리프레시 실패 시 토큰을 null로 설정하여 인증 시도를 중단
                    jwtToken = null;
                }
            } catch (JwtException e) {
                log.warn("Invalid JWT Token received: {}", e.getMessage());
                jwtToken = null;
            } catch (IllegalArgumentException e) {
                log.warn("Unable to get JWT Token: {}", e.getMessage());
                jwtToken = null;
            }
        }

        // Access Token이 초기에 없었거나, 만료 후 리프레시가 실패했을 경우 리프레시 시도
        if (jwtToken == null) {
            log.debug("No valid Access Token found. Attempting to refresh token from cookie.");
            String newAccessToken = attemptTokenRefresh(request, response);
            if (newAccessToken != null) {
                jwtToken = newAccessToken;
                try {
                    username = jwtTokenProvider.getUserId(jwtToken);
                    userAuthrt = jwtTokenProvider.getClaimValue(jwtToken, "userAuthrt");
                } catch (Exception ex) {
                    log.error("Failed to extract user info from newly refreshed access token (initial null case): {}", ex.getMessage());
                    jwtToken = null;
                }
            }
        }

        // 4. Redis 세션 검증 (세션 존재 + 토큰 해시 일치)
        if (username != null && jwtToken != null) {
            if (!sessionService.validateSession(username, jwtToken)) {
                log.warn("세션 검증 실패 - 세션 없음 또는 토큰 불일치: userId={}", username);
                cookieUtil.deleteAuthCookies(response);
                jwtToken = null;
                username = null;
            }
        }

        // 5. 인증 처리
        if (username != null && jwtToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                if (jwtTokenProvider.validateToken(jwtToken, "access")) {
                    // loginSessionId를 토큰에서 추출하여 BaseUser에 설정
                    if (userDetails instanceof BaseUser) {
                        BaseUser baseUser = (BaseUser) userDetails;
                        String loginSessionId = jwtTokenProvider.getLoginSessionId(jwtToken);
                        baseUser.setLoginSessionId(loginSessionId);
                        log.debug("[LoginSessionId] SET in BaseUser from token: {} (userId: {})", loginSessionId, username);
                    }

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("User authenticated: {}", username);
                }
            } catch (org.springframework.security.authentication.AuthenticationServiceException e) {
                // DB 타임아웃 등 서버 내부 오류 → 쿠키 유지, 다음 요청에서 재시도
                log.error("Internal error during authentication for user '{}': {}", username, e.getMessage());
            } catch (org.springframework.security.core.AuthenticationException e) {
                // UsernameNotFoundException, DisabledException, LockedException 등 → 재시도 무의미
                log.warn("Authentication rejected for user '{}': {}", username, e.getMessage());
                cookieUtil.deleteAuthCookies(response);
            } catch (JwtException e) {
                // 토큰 자체가 유효하지 않음
                log.warn("Invalid token for user '{}': {}", username, e.getMessage());
                cookieUtil.deleteAuthCookies(response);
            } catch (Exception e) {
                // 기타 예상치 못한 오류 → 쿠키 유지
                log.error("Unexpected error during authentication for user '{}': {}", username, e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }

    private String attemptTokenRefresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtil.getRefreshToken(request);
        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                // JWT 유효성 검증
                if (refreshTokenService.validateRefreshToken(refreshToken)) {
                    String refreshUserId = jwtTokenProvider.getUserId(refreshToken);
                    String refreshTokenId = jwtTokenProvider.getTokenId(refreshToken);

                    // 세션의 refreshTokenId와 일치하는지 확인
                    if (!sessionService.validateRefreshToken(refreshUserId, refreshTokenId)) {
                        log.warn("Refresh Token이 세션과 불일치. 다른 곳에서 로그인됨. userId={}", refreshUserId);
                        cookieUtil.deleteAuthCookies(response);
                        return null;
                    }
                    BaseUser user = authService.getActiveUserById(refreshUserId);

                    if (user != null) {
                        String userAuthrt = user.getUserAuthrt();
                        String loginSessionId = jwtTokenProvider.getClaimValue(refreshToken, "loginSessionId");
                        String newAccessToken = jwtTokenProvider.generateAccessToken(user, loginSessionId);
                        String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);

                        long accessTokenExp = jwtTokenProvider.getAccessTokenExpiration(userAuthrt);
                        long refreshTokenExp = jwtTokenProvider.getRefreshTokenExpiration(userAuthrt);

                        // Redis 세션 업데이트 (새 토큰 정보로)
                        String newRefreshTokenId = jwtTokenProvider.getTokenId(newRefreshToken);
                        sessionService.updateSession(refreshUserId, userAuthrt, newAccessToken, newRefreshTokenId);

                        response.addHeader("Set-Cookie", cookieUtil.createAccessTokenCookie(newAccessToken, (int) (accessTokenExp / 1000)));
                        response.addHeader("Set-Cookie", cookieUtil.createRefreshTokenCookie(newRefreshToken, (int) (refreshTokenExp / 1000)));
                        log.info("Access token refreshed successfully for user: {} (role: {})", refreshUserId, userAuthrt);
                        return newAccessToken;
                    } else {
                        log.warn("User not found for refresh token: {}. Deleting auth cookies.", refreshUserId);
                        cookieUtil.deleteAuthCookies(response);
                    }
                } else {
                    log.warn("Invalid refresh token. Deleting auth cookies.");
                    cookieUtil.deleteAuthCookies(response);
                }
            } catch (ExpiredJwtException refreshExpiredException) {
                log.warn("Refresh token has expired. User needs to re-login. Deleting auth cookies.");
                cookieUtil.deleteAuthCookies(response);
            } catch (Exception refreshException) {
                log.error("Error during token refresh: {}", refreshException.getMessage());
                cookieUtil.deleteAuthCookies(response);
            }
        } else {
            log.debug("No refresh token found in cookies.");
        }
        return null;
    }

    /**
     * 리소스 요청 여부 판단 (CSS, JS, 이미지 등)
     * SecurityConstants에 정의된 리소스 패턴과 매칭
     */
    private boolean isResourceRequest(String rqtUri) {
        for (String pattern : SecurityConstants.ALL_RESOURCES) {
            if (antPathMatcher.match(pattern, rqtUri)) {
                return true;
            }
        }
        return false;
    }
}
