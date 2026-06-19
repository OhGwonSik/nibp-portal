package egovframework.common.jwt;

import egovframework.common.util.RequestTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

/**
 * @ClassName : JwtAuthenticationEntryPoint.java
 * @Description : 인증되지 않은 사용자가 보호된 리소스에 접근 시 401 Unauthorized 에러를 반환하는 클래스
 *
 * @author : 표준프레임워크센터
 * @since  : 2023. 07. 28
 * @version : 1.0
 *
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = -7858869558953243875L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // RequestTypeUtil을 사용하여 API 요청 판단 (/api/, /auth/ 등)
        boolean isApi = RequestTypeUtil.isApiRequest(request);

        log.warn("JwtAuthenticationEntryPoint - URI: {}, isApi: {}", request.getRequestURI(), isApi);

        // RequestTypeUtil을 사용하여 API 요청 판단 (/api/, /auth/ 등)
        if(isApi){
            // 유효한 자격증명을 제공하지 않고 접근하려 할때 401
            log.debug("Sending 401 Unauthorized for API request: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        } else {
            // 현재 요청 URL을 쿼리 파라미터로 전달
            String requestURI = request.getRequestURI();
            String queryString = request.getQueryString();
            String returnUrl = queryString != null ? requestURI + "?" + queryString : requestURI;

            if (requestURI.startsWith("/admin")) {
                log.debug("Redirecting to /admin/login?returnUrl={}", returnUrl);
                response.sendRedirect("/admin/login?returnUrl=" + java.net.URLEncoder.encode(returnUrl, "UTF-8"));
            } else {
                log.debug("Redirecting to /login?returnUrl={}", returnUrl);
                response.sendRedirect("/login?returnUrl=" + java.net.URLEncoder.encode(returnUrl, "UTF-8"));
            }
        }
    }
}
