package egovframework.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FileAccessLogInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestUri = request.getRequestURI();
        int status = response.getStatus();

        if (ex != null) {
            log.error("Exception occurred while serving file resource: URI={}, status={}", requestUri, status, ex);
        } else if (status >= 200 && status < 300) {
            log.info("File resource served successfully: URI={}, status={}", requestUri, status);
        } else {
            switch (status) {
                case HttpServletResponse.SC_FORBIDDEN:
                    log.warn("Access denied to file resource: URI={}, status={}", requestUri, status);
                    break;
                case HttpServletResponse.SC_NOT_FOUND:
                    log.warn("File not found: URI={}, status={}", requestUri, status);
                    break;
                default:
                    if (status >= 400 && status < 500) {
                        log.warn("Client error while serving file resource: URI={}, status={}", requestUri, status);
                    } else if (status >= 500 && status < 600) {
                        log.error("Server error while serving file resource: URI={}, status={}", requestUri, status);
                    } else {
                        log.warn("Failed to serve file resource with unknown status: URI={}, status={}", requestUri, status);
                    }
                    break;
            }
        }
    }
}
