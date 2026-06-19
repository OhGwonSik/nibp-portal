package egovframework.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName : CustomAccessDeniedHandler.java
 * @Description : 권한 없는 접근 시 처리 핸들러
 *
 * @author : tspark
 * @since : 2025. 11. 05
 * @version : 1.0
 *
 *          <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일              수정자               수정내용
 *  -------------  ------------   ---------------------
 *   2025. 11. 05    tspark          최초 생성
 *          </pre>
 */
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestUri = request.getRequestURI();

        log.warn("Access denied for user: {} on URI: {}",
                authentication != null ? authentication.getName() : "anonymous",
                requestUri);

        boolean isAdmin = authentication != null &&
                          authentication.getAuthorities().stream()
                              .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isApi = requestUri.startsWith("/api/");

        // 관리자 페이지 접근 시도
        if (requestUri.startsWith("/admin") && !isAdmin) {
            if (isApi) {
                // API 요청인 경우 JSON 응답
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Access Denied\",\"message\":\"관리자 권한이 필요합니다.\"}");
            } else {
                // 페이지 요청인 경우 리다이렉트
                response.sendRedirect("/main");
            }
            return;
        }

        // 그 외의 경우 403 응답
        if (isApi) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Access Denied\"}");
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
        }
    }
}
