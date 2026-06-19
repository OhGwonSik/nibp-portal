package egovframework.common.interceptor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    @Value("${app.rate-limit.max-requests:5}")
    private int maxRequests;

    @Value("${app.rate-limit.expire-seconds:5}")
    private int expireSeconds;

    private Cache<String, AtomicInteger> requestCounts;

    @PostConstruct
    public void init() {
        requestCounts = Caffeine.newBuilder()
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();
        String normalizedUri = normalizeUri(uri);
        String key = ip + ":" + normalizedUri;

        log.debug("RateLimitingInterceptor: original={}, normalized={}, key={}", uri, normalizedUri, key);

        AtomicInteger count = requestCounts.get(key, k -> new AtomicInteger(0));
        int currentRequests = count.incrementAndGet();

        if (currentRequests > maxRequests) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("너무 많은 요청이 발생했습니다.\n\n" + expireSeconds + "초 후에 다시 시도해주세요.");
            log.warn("Rate limit exceeded: {} (count: {})", key, currentRequests);
            return false;
        }

        return true;
    }

    /**
     * URI 패턴 정규화
     */
    private String normalizeUri(String uri) {
        return uri;
    }
}
