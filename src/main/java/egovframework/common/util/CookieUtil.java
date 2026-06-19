package egovframework.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

/**
 * @ClassName : CookieUtil.java
 * @Description : 쿠키 유틸리티
 *
 * @author : tspark
 * @since  : 2025. 11. 04
 * @version : 1.0
 */
@Component
public class CookieUtil {

    private static final String ACCESS_TOKEN_BASE_NAME = "nibp-accessToken";
    private static final String REFRESH_TOKEN_BASE_NAME = "nibp-refreshToken";
    public static final String ANONYMOUS_LOGIN_SESSION_ID_COOKIE_NAME = "_nibp_sid"; // 익명 사용자 loginSessionId 쿠키 이름

    @Value("${cookie.secure:true}")
    private boolean cookieSecure;

    // secure 플래그에 따라 prefix 자동 결정
    private String getCookieNamePrefix() {
        return cookieSecure ? "__Host-" : "";
    }

    // 동적으로 쿠키 이름 생성
    private String getAccessTokenCookieName() {
        return getCookieNamePrefix() + ACCESS_TOKEN_BASE_NAME;
    }

    private String getRefreshTokenCookieName() {
        return getCookieNamePrefix() + REFRESH_TOKEN_BASE_NAME;
    }

    /**
     * 쿠키 생성
     * @param name 쿠키 이름
     * @param value 쿠키 값
     * @param maxAge 만료 시간 (초)
     * @param httpOnly HttpOnly 플래그
     * @param secure Secure 플래그 (HTTPS only)
     * @param path 경로
     * @return Set-Cookie 헤더 문자열
     */
    public String createCookie(String name, String value, int maxAge, boolean httpOnly, boolean secure, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .maxAge(Duration.ofSeconds(maxAge))
                .httpOnly(httpOnly)
                .secure(secure)
                .path(path)
                .sameSite("Lax")
                .build();
        return cookie.toString();
    }

    /**
     * Access Token 쿠키 생성
     * @param accessToken Access Token
     * @param maxAge 만료 시간 (초)
     * @return Set-Cookie 헤더 문자열
     */
    public String createAccessTokenCookie(String accessToken, int maxAge) {
        // 환경 설정에 따라 secure 플래그 및 쿠키 이름 적용
        return createCookie(getAccessTokenCookieName(), accessToken, maxAge, true, cookieSecure, "/");
    }

    /**
     * Refresh Token 쿠키 생성
     * @param refreshToken Refresh Token
     * @param maxAge 만료 시간 (초)
     * @return Set-Cookie 헤더 문자열
     */
    public String createRefreshTokenCookie(String refreshToken, int maxAge) {
        // 환경 설정에 따라 secure 플래그 및 쿠키 이름 적용
        return createCookie(getRefreshTokenCookieName(), refreshToken, maxAge, true, cookieSecure, "/");
    }

    /**
     * 익명 사용자 loginSessionId 쿠키를 생성합니다.
     * @param loginSessionId 로그인 세션 ID
     * @param maxAge 쿠키 만료 시간 (초)
     * @return Set-Cookie 헤더 문자열
     */
    public String createAnonymousLoginSessionIdCookie(String loginSessionId, int maxAge) {
        return createCookie(ANONYMOUS_LOGIN_SESSION_ID_COOKIE_NAME, loginSessionId, maxAge, true, false, "/");
    }

    /**
     * 쿠키 삭제 (만료)
     * @param name 쿠키 이름
     * @param path 경로
     * @param secure Secure 플래그 (HTTPS only)
     * @param httpOnly HttpOnly 플래그
     * @return Set-Cookie 헤더 문자열
     */
    public String deleteCookie(String name, String path, boolean secure, boolean httpOnly) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .maxAge(0)
                .path(path)
                .secure(secure)
                .httpOnly(httpOnly)
                .sameSite("Lax")
                .build();
        return cookie.toString();
    }

    /**
     * 쿠키 값 조회
     * @param request HttpServletRequest
     * @param name 쿠키 이름
     * @return 쿠키 값 (없으면 null)
     */
    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Access Token 쿠키 조회
     * @param request HttpServletRequest
     * @return Access Token (없으면 null)
     */
    public static String getAccessToken(HttpServletRequest request) {
        // HTTP: nibp-accessToken, HTTPS: __Host-nibp-accessToken 둘 다 시도
        String token = getCookieValue(request, ACCESS_TOKEN_BASE_NAME);
        if (token == null) {
            token = getCookieValue(request, "__Host-" + ACCESS_TOKEN_BASE_NAME);
        }
        return token;
    }

    /**
     * Refresh Token 쿠키 조회
     * @param request HttpServletRequest
     * @return Refresh Token (없으면 null)
     */
    public static String getRefreshToken(HttpServletRequest request) {
        // HTTP: nibp-refreshToken, HTTPS: __Host-nibp-refreshToken 둘 다 시도
        String token = getCookieValue(request, REFRESH_TOKEN_BASE_NAME);
        if (token == null) {
            token = getCookieValue(request, "__Host-" + REFRESH_TOKEN_BASE_NAME);
        }
        return token;
    }

    /**
     * 익명 사용자 loginSessionId 쿠키 조회
     * @param request HttpServletRequest
     * @return loginSessionId (없으면 null)
     */
    public static String getAnonymousLoginSessionId(HttpServletRequest request) {
        return getCookieValue(request, ANONYMOUS_LOGIN_SESSION_ID_COOKIE_NAME);
    }

    /**
     * 모든 인증 쿠키 삭제
     * @param response HttpServletResponse
     */
    public void deleteAuthCookies(HttpServletResponse response) {
        response.addHeader("Set-Cookie", deleteCookie(getAccessTokenCookieName(), "/", cookieSecure, true));
        response.addHeader("Set-Cookie", deleteCookie(getRefreshTokenCookieName(), "/", cookieSecure, true));
        response.addHeader("Set-Cookie", deleteCookie(ANONYMOUS_LOGIN_SESSION_ID_COOKIE_NAME, "/", false, true)); // _nibp_sid 삭제
    }
}