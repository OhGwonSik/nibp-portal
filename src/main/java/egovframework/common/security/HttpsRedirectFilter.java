package egovframework.common.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * X-Forwarded-Proto 헤더 기반 HTTP → HTTPS 리다이렉트 필터.
 *
 * 리버스 프록시가 X-Forwarded-Proto: http 헤더를 전달할 때만
 * 동일 URL의 HTTPS 버전으로 301 리다이렉트한다.
 * 헤더가 없거나 https인 경우(Docker 내부 헬스체크 등)는 그대로 통과한다.
 */
@Slf4j
public class HttpsRedirectFilter extends OncePerRequestFilter {

    private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String proto = request.getHeader(X_FORWARDED_PROTO);

        if ("http".equalsIgnoreCase(proto)) {
            String host = request.getHeader("Host");
            String requestUri = request.getRequestURI();
            String queryString = request.getQueryString();

            String redirectUrl = "https://" + host + requestUri;
            if (queryString != null) {
                redirectUrl += "?" + queryString;
            }

            log.debug("HTTP → HTTPS redirect: {}", redirectUrl);
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", redirectUrl);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
