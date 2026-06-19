package egovframework.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName : LoginPageRedirectFilter.java
 * @Description : 로그인된 사용자가 로그인 페이지 접근 시 리다이렉트 처리
 *
 * @author : tspark
 * @since  : 2025. 11. 05
 * @version : 1.0
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일              수정자               수정내용
 *  -------------  ------------   ---------------------
 *   2025. 11. 05    tspark          최초 생성
 * </pre>
 */
@Slf4j
public class LoginPageRedirectFilter extends OncePerRequestFilter {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String rqtUri = request.getRequestURI();

        // 리소스 요청 제외 (CSS, JS, 이미지 등)
        if (isResourceRequest(rqtUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.debug("LoginPageRedirectFilter - URI: {}, Authenticated: {}, Principal: {}",
                rqtUri,
                authentication != null && authentication.isAuthenticated(),
                authentication != null ? authentication.getPrincipal() : "null");

        // 인증된 사용자가 로그인 페이지에 접근하는 경우
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {

            // 로그인 포털 로그인 페이지 접근 시 메인으로 리다이렉트
            if ("/login".equals(rqtUri)) {
                log.debug("Authenticated user accessing /login, redirecting to /main");
                response.sendRedirect("/main");
                return;
            }

            // 관리자 권한 로그인 되어있을 경우 관리자 로그인 페이지 접근 시 관리자 메인으로 리다이렉트
            if ("/admin/login".equals(rqtUri)) {
                if(authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))){
                    log.debug("Authenticated admin user accessing /admin/login, redirecting to /admin/main");
                    response.sendRedirect("/admin/main");
                    return;
                } else {
                    log.debug("Authenticated not-admin user accessing /admin/login, redirecting to /main");
                    response.sendRedirect("/main");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
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
