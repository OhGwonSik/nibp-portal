package egovframework.common.util;

import lombok.experimental.UtilityClass;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName : RequestTypeUtil.java
 * @Description : HTTP 요청 타입 판단 유틸리티
 *
 * @author : tspark
 * @since  : 2025. 11. 13
 * @version : 1.0
 */
@UtilityClass
public class RequestTypeUtil {
    /**
     * API 요청인지 판단
     * 다음 조건 중 하나를 만족하면 API 요청으로 간주:
     * 1. 요청 경로가 /api/로 시작
     * 2. 인증 관련 요청 (/auth/)
     * 3. Accept 헤더에 application/json 포함
     * 4. Content-Type이 application/json 포함
     */
    public static boolean isApiRequest(HttpServletRequest request) {
        // 1. /api/ 경로는 API 요청
        if (isApiPath(request.getRequestURI())) {
            return true;
        }

        // 2. /auth/ 경로는 인증 API 요청
        if (isAuthRequest(request.getRequestURI())) {
            return true;
        }

        // 3. Accept 헤더에 application/json이 있으면 API 요청
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            return true;
        }

        // 4. Content-Type이 application/json이면 API 요청
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            return true;
        }

        return false;
    }

    /**
     * 페이지 요청인지 판단
     */
    public static boolean isPageRequest(HttpServletRequest request) {
        return !isApiRequest(request);
    }

    /**
     * API 경로인지 판단 (/api/로 시작)
     */
    private static boolean isApiPath(String requestUri) {
        return requestUri != null && requestUri.contains("/api/");
    }

    /**
     * 인증 관련 요청인지 판단 (/auth/로 시작)
     * 로그인, 회원가입, 토큰 갱신 등의 요청
     */
    private static boolean isAuthRequest(String requestUri) {
        return requestUri != null && requestUri.contains("/auth/");
    }

    /**
     * 관리자 페이지 요청인지 판단 (/admin/page/로 시작)
     */
    public static boolean isAdminPageRequest(String path) {
        return path != null && path.startsWith("/admin/page/");
    }

    /**
     * 포탈 페이지 요청인지 판단 (/page/로 시작)
     */
    public static boolean isPortalPageRequest(String path) {
        return path != null && path.startsWith("/page/");
    }

    /**
     * 게시판 페이지 요청인지 판단 (/board/로 시작)
     */
    public static boolean isBoardPageRequest(String path) {
        return path != null && path.startsWith("/board");
    }

    /**
     * Context-path를 제거한 실제 요청 경로 반환
     */
    public static String getActualPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        return requestUri.substring(contextPath.length());
    }
}
