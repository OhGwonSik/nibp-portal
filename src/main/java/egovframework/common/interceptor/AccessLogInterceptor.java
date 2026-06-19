package egovframework.common.interceptor;

import egovframework.common.audit.domain.ApiAccessLog;
import egovframework.common.audit.service.AuditService;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.auth.service.AuthService;
import egovframework.common.constant.Constants;
import egovframework.common.jwt.JwtTokenProvider;
import egovframework.common.util.CookieUtil;
import egovframework.common.util.CryptoUtil;
import egovframework.common.util.RequestUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessLogInterceptor implements HandlerInterceptor {
    private final AuditService auditService; // audit
    private final JwtTokenProvider jwtTokenProvider; // jwt
    private final AuthService authService; // 인증
    private final CryptoUtil cryptoUtil; // 암호화
    private final CookieUtil cookieUtil;


    private static final String START_TIME_ATTRIBUTE = "startTime";
    private static final String ACCESS_LOG_ATTRIBUTE = "apiAccessLog";
    private static final int ANONYMOUS_LOGIN_SESSION_ID_MAX_AGE = 60 * 60 * 24 * 30; // 30 days

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        String rqtUri = request.getRequestURI();
        String ipAddress = RequestUtil.getRemoteIpAddress();

        // JWT에서 userId, loginSessionId 추출
        String userId = null;
        String loginSessionId = null;
        String loginSessionIdSource = null; // 디버깅용: loginSessionId 출처

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof BaseUser) {
            BaseUser principal = (BaseUser) authentication.getPrincipal();
            userId = principal.getUserId();
            loginSessionId = principal.getLoginSessionId(); // BaseUser에서 loginSessionId 가져오기
            loginSessionIdSource = "BaseUser";
            log.info("[LoginSessionId] from BaseUser: {} (userId: {})", loginSessionId, userId);
        } else {
            // SecurityContextHolder에 없으면 토큰에서 직접 파싱 시도 (예: 비로그인 상태에서 토큰만 있는 경우)
            String token = CookieUtil.getAccessToken(request); // CookieUtil에서 Access Token 가져오기
            if (token != null) {
                try {
                    userId = jwtTokenProvider.getUserId(token);
                    loginSessionId = jwtTokenProvider.getLoginSessionId(token);
                    loginSessionIdSource = "Token";
                    log.info("[LoginSessionId] from Token: {} (userId: {})", loginSessionId, userId);
                } catch (Exception e) {
                    log.debug("Failed to parse token in AccessLogInterceptor: {}", e.getMessage());
                }
            }
        }

        // userId가 null이면 "ANONYMOUS"로 설정
        if (userId == null) {
            userId = "ANONYMOUS";
        }

        // loginSessionId가 null이면 쿠키에서 찾거나 새로 생성
        if (loginSessionId == null) {
            loginSessionId = CookieUtil.getAnonymousLoginSessionId(request); // 쿠키에서 loginSessionId 조회
            if (loginSessionId == null) {
                loginSessionId = jwtTokenProvider.generateLoginSessionId(); // 새로운 loginSessionId 생성
                loginSessionIdSource = "_app_sid (new)";
                log.info("[LoginSessionId] NEW _app_sid generated: {} (userId: ANONYMOUS)", loginSessionId);
                // 새로 생성된 loginSessionId를 쿠키에 추가하여 클라이언트에게 전달
                response.addHeader("Set-Cookie" ,cookieUtil.createAnonymousLoginSessionIdCookie(loginSessionId, ANONYMOUS_LOGIN_SESSION_ID_MAX_AGE));
            } else {
                loginSessionIdSource = "_app_sid (cookie)";
                log.info("[LoginSessionId] from _app_sid cookie: {} (userId: {})", loginSessionId, userId);
            }
        }

        Long userOid = null;
        if (!"ANONYMOUS".equals(userId)) { // ANONYMOUS가 아니면 userOid 조회
            BaseUser user = authService.getUserById(userId);
            if (user != null) {
                userOid = user.getUserOid();
            }
        }

        // AccessType 결정
        String acsType = "";
        if (rqtUri.startsWith("/api")) {
            acsType = "API";
        } else if (rqtUri.startsWith("/files")) {
            acsType = "FILE";
        } else {
            acsType = "PAGE";
        }

        // Referrer 및 UTM 파라미터 추출
        String rfprUrl = RequestUtil.getReferrerUrl(request);
        String rfprType = RequestUtil.getReferrerType(request);
        String utmSource = request.getParameter("utm_source");
        String userAgt = request.getParameter("utm_medium");
        String brwsrNm = request.getParameter("utm_campaign");
        String brwsrVer = request.getParameter("utm_content");
        String utmTerm = request.getParameter("utm_term");

        String extractedSrchKywd = null;

        // 1. Attempt to extract keyword from external search engine referrer
        String currentHost = request.getServerName();
        if (rfprUrl != null && !rfprUrl.isEmpty()) {
            try {
                java.net.URL url = new java.net.URL(rfprUrl);
                String referrerHost = url.getHost().toLowerCase();
                String query = url.getQuery();

                // Only process external referrers that are not from the current site
                if (query != null && !referrerHost.equals(currentHost)) {
                    Map<String, String> queryParams = parseQueryString(query); // Use the helper method

                    if (referrerHost.contains("google.")) {
                        extractedSrchKywd = queryParams.get("q");
                    } else if (referrerHost.contains("naver.")) {
                        extractedSrchKywd = queryParams.get("query");
                    } else if (referrerHost.contains("bing.") || referrerHost.contains("daum.")) {
                        extractedSrchKywd = queryParams.get("q");
                    } else if (referrerHost.contains("yahoo.")) {
                        extractedSrchKywd = queryParams.get("p");
                    }
                    // Add more search engines as needed
                }
            } catch (Exception e) {
                log.debug("Failed to parse referrer URL for search keyword: {}", rfprUrl, e);
            }
        }

        // 2. If no external keyword, try to get from internal search parameter
        if (extractedSrchKywd == null || extractedSrchKywd.isEmpty()) {
            extractedSrchKywd = request.getParameter("srchKywd");
        }

        ApiAccessLog apiAccessLog = ApiAccessLog.builder()
            .userId(userId)
            .userOid(userOid)
            .acsType(acsType)
            .rqtUri(rqtUri)
            .rqtMthd(request.getMethod())
            .rqtQry(request.getQueryString())
            .ipAddr(ipAddress)
            .userAgt(RequestUtil.getUserAgent())
            .brwsrNm(RequestUtil.getBrowserName())
            .brwsrVer(RequestUtil.getBrowserVersion())
            .osNm(RequestUtil.getOs())
            .osVer(RequestUtil.getOsVersion())
            .dvcType(RequestUtil.getDeviceType())
            .ntnCd(null)
            .ctyNm(null)
            .ssnId(loginSessionId)
            .rfprUrl(rfprUrl)
            .rfprType(rfprType)
            .srchKywd(extractedSrchKywd)
            .regDt(LocalDateTime.now())
            .regId(userId)
            .build();

        // 암호화하지 않고 평문 객체로 저장 (afterCompletion에서 수정 후 암호화)
        request.setAttribute(ACCESS_LOG_ATTRIBUTE, apiAccessLog);

        return true;
    }

    /**
     * URL 쿼리 문자열을 파싱하여 Map<String, String> 형태로 반환합니다.
     * @param queryString 파싱할 쿼리 문자열 (예: "key1=value1&key2=value2")
     * @return 파싱된 쿼리 파라미터 맵
     */
    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> queryParams = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return queryParams;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx != -1) {
                try {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
                    queryParams.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    log.warn("Error decoding query parameter: {}", pair, e);
                }
            }
        }
        return queryParams;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 추후 필요시 로직 추가
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ApiAccessLog apiAccessLog = (ApiAccessLog) request.getAttribute(ACCESS_LOG_ATTRIBUTE);
        if (apiAccessLog == null) {
            return;
        }

        // AuthController에서 설정한 loginSessionId가 있으면 업데이트
        String controllerLoginSessionId = (String) request.getAttribute("LOGIN_SESSION_ID");
        if (controllerLoginSessionId != null) {
            String oldLoginSessionId = apiAccessLog.getSsnId();
            apiAccessLog.setSsnId(controllerLoginSessionId);
            log.info("[LoginSessionId] UPDATED in afterCompletion: {} -> {} (from AuthController)", oldLoginSessionId, controllerLoginSessionId);
        }

        long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long endTime = System.currentTimeMillis();
        long rspnsTimeMs = endTime - startTime;

        apiAccessLog.setSttsCd(response.getStatus());
        apiAccessLog.setRspnsTimeMs(rspnsTimeMs);
        apiAccessLog.setRegDt(LocalDateTime.now());
        apiAccessLog.setRegId(apiAccessLog.getUserId() != null ? apiAccessLog.getUserId() : Constants.SYSTEM_ID);

        if (ex != null) {
            apiAccessLog.setErrorMsg(ex.getMessage());
        }

        apiAccessLog.setRqtBdySz(0); // ContentCachingResponseWrapper가 필요하므로 일단 0으로 처리
        apiAccessLog.setRspnsBdySz(0); // ContentCachingResponseWrapper가 필요하므로 일단 0으로 처리

        // 모든 수정 완료 후 암호화하여 저장
        auditService.insertAccessLog(cryptoUtil.encrypt(apiAccessLog));
    }
}