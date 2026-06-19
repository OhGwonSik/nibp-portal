package egovframework.common.util;

import egovframework.common.enums.DeviceType;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import eu.bitwalker.useragentutils.Version;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
@Slf4j
public class RequestUtil {
private static final String[] IP_HEADERS = {
	        "X-Forwarded-For",
	        "Proxy-Client-IP",
	        "WL-Proxy-Client-IP",
	        "HTTP_X_FORWARDED_FOR",
	        "HTTP_X_FORWARDED",
	        "HTTP_X_CLUSTER_CLIENT_IP",
	        "HTTP_CLIENT_IP",
	        "HTTP_FORWARDED_FOR",
	        "HTTP_FORWARDED",
	        "HTTP_VIA",
	        "REMOTE_ADDR"
	};

    /**
     * 현재 요청의 HttpServletRequest를 반환합니다.
     * @return HttpServletRequest
     */
    public static HttpServletRequest getHttpServletRequest() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            return null;
        }
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }
    
    /**
     * 클라이언트의 IP 주소를 반환합니다.
     * @return 클라이언트의 IP 주소
     */
    public static String getRemoteIpAddress() {
        HttpServletRequest request = getHttpServletRequest();
        if(request == null){
            return "";
        }

        String ip = request.getHeader("X-Forwarded-For");

        // X-Forwarded-For 헤더를 확인합니다.
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        
        // Proxy-Client-IP 헤더를 확인합니다. (주로 WebLogic에서 사용)
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        
        // HTTP_CLIENT_IP 헤더를 확인합니다.
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        
        // HTTP_X_FORWARDED_FOR 헤더를 확인합니다.
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        
        // 위의 헤더들에서도 IP를 찾지 못하면 getRemoteAddr()을 사용합니다.
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 'X-Forwarded-For' 헤더에 여러 개의 IP가 콤마로 구분되어 있을 경우, 
        // 가장 앞의 IP가 실제 클라이언트 IP입니다.
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    /**
     * 클라이언트의 User-Agent를 반환합니다.
     * @return User-Agent
     */
    public static String getUserAgent() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (attributes == null || !(attributes instanceof ServletRequestAttributes)) {
            return "";
        }
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();

        return request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : "";
    }

    /**
     * 브라우저 이름을 반환합니다.
     * @return 브라우저 이름 (예: Chrome, Firefox, Safari 등)
     */
    public static String getBrowserName() {
        String userAgent = getUserAgent();
        
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        UserAgent agent = UserAgent.parseUserAgentString(userAgent);
        if (agent == null) {
            return "Unknown";
        }
        
        Browser browser = agent.getBrowser();
        return (browser != null) ? browser.getName() : "Unknown";
    }

    /**
     * 브라우저 버전을 반환합니다.
     * @return 브라우저 버전 (예: Chrome 120.0.0.0)
     */
    public static String getBrowserVersion() {
        // return "";
        try {
            String userAgent = getUserAgent();
            
            if (userAgent == null || userAgent.isEmpty()) {
                return "Unknown";
            }
            
            UserAgent agent = UserAgent.parseUserAgentString(userAgent);
            if (agent == null) {
                return "Unknown";
            }
            
            Version version = agent.getBrowserVersion();
            if (version == null) {
                return "Unknown";
            }
            
            String versionString = version.getVersion();
            return (versionString != null && !versionString.isEmpty()) ? versionString : "Unknown";
            
        } catch (NullPointerException e) {
            return "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * 운영체제 정보를 반환합니다 (버전 제외).
     * @return 운영체제 정보 (예: Windows, Mac OS X, Linux 등)
     */
    public static String getOs() {
        String userAgent = getUserAgent();
        
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        try {
            UserAgent agent = UserAgent.parseUserAgentString(userAgent);
            OperatingSystem os = agent.getOperatingSystem();
            
            if (os != null) {
                String osName = os.getName();
                if (osName != null) {   
                    return osName.replaceAll("\\s*\\d+.*$", 
                    "").trim();
                }
                return osName;
            }
        } catch (Exception e) {
            log.error("Error getting OS name: " + e.getMessage(), e);
        }
        
        return "Unknown";
    }

    /**
     * 운영체제 버전을 반환합니다.
     * @return 운영체제 버전 (예: 10, 11, 10.15 등)
     */
    public static String getOsVersion() {
        String userAgent = getUserAgent();
        
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        try {
            UserAgent agent = UserAgent.parseUserAgentString(userAgent);
            OperatingSystem os = agent.getOperatingSystem();
            
            if (os != null) {
                String osName = os.getName();
                if (osName != null) {
                    Pattern pattern = Pattern.compile("\\d+(\\.\\d+)*");
                    Matcher matcher = pattern.matcher(osName);
                    if (matcher.find()) {
                        return matcher.group();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing OS version: " + e.getMessage(), e);
        }
        
        return "Unknown";
    }
    
    /**
     * 디바이스 유형을 반환합니다.
     * @return DeviceType enum (PC, MOBILE, TABLET, ETC)
     */
    public static DeviceType getDeviceType() {
        String userAgent = getUserAgent();
        
        if (userAgent == null || userAgent.isEmpty()) {
            return DeviceType.ETC;
        }
        
        try {
            UserAgent agent = UserAgent.parseUserAgentString(userAgent);
            OperatingSystem os = agent.getOperatingSystem();
            
            if (os != null) {
                String deviceType = os.getDeviceType().getName().toUpperCase();
                switch (deviceType) {
                    case "COMPUTER":
                        return DeviceType.PC;
                    case "MOBILE":
                        return DeviceType.MOBILE;
                    case "TABLET":
                        return DeviceType.TABLET;
                    default:
                        return DeviceType.ETC;
                }
            }
        } catch (Exception e) {
            log.error("Error getting device type: " + e.getMessage(), e);
        }
        
        return DeviceType.ETC;
    }

    /**
     * Referer URL을 반환합니다.
     * @param request HttpServletRequest
     * @return Referer URL
     */
    public static String getReferrerUrl(HttpServletRequest request) {
        return request.getHeader("Referer");
    }

    /**
     * Referer URL을 기반으로 유입 유형을 반환합니다.
     * @param request HttpServletRequest
     * @return 유입 유형 (Direct, Search Engine, Social Media, External Website)
     */
    public static String getReferrerType(HttpServletRequest request) {
        String referrerUrl = getReferrerUrl(request);

        if (referrerUrl == null || referrerUrl.isEmpty()) {
            return "Direct";
        }

        // 현재 요청의 호스트와 Referer의 호스트가 같은지 확인
        String currentHost = request.getServerName();
        if (referrerUrl.contains(currentHost)) {
            return "Internal";
        }

        // 검색 엔진 분류
        if (referrerUrl.contains("google.com") || referrerUrl.contains("naver.com") ||
            referrerUrl.contains("daum.net") || referrerUrl.contains("bing.com")) {
            return "Search Engine";
        }

        // 소셜 미디어 분류
        if (referrerUrl.contains("facebook.com") || referrerUrl.contains("instagram.com") ||
            referrerUrl.contains("twitter.com") || referrerUrl.contains("youtube.com")) {
            return "Social Media";
        }

        // 그 외 외부 사이트
        return "External Website";
    }

    /**
     * HttpServletRequest에서 Access Token을 추출합니다.
     * Authorization 헤더 또는 쿠키에서 찾습니다.
     * @param request HttpServletRequest
     * @return Access Token 문자열 또는 null
     */
    public static String getAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        
        return null;
    }
}
