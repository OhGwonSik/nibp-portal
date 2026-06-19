package egovframework.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import egovframework.common.auth.domain.BaseUser;

@ControllerAdvice
public class GlobalModelAttribute {

    @Value("${file.file-path}")
    private String filePath;

    @Value("${file.defaultMaxFileSizeMb}")
    private String maxFileSize;

    @Value("${file.allowedExtensions:pdf,hwp,hwpx,doc,docx,ppt,pptx,xls,xlsx,txt,zip,jpg,jpeg,png,gif,bmp}")
    private String allowedExtensions;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    // Role별 Access Token 만료 시간
    @Value("${jwt.access-token.expiration.user:10800000}")
    private Long userAccessTokenExpiration;

    @Value("${jwt.access-token.expiration.admin:1800000}")
    private Long adminAccessTokenExpiration;

    @ModelAttribute("basicFilePath")
    public String filePath() {
        return filePath;
    }

    @ModelAttribute("maxFileSize")
    public String maxFileSize(){
        return maxFileSize;
    }

    @ModelAttribute("allowedExtensions")
    public String allowedExtensions() {
        return allowedExtensions;
    }

    @ModelAttribute("ckeditorBasePath")
    public String ckeditorBasePath() {
        String base = contextPath.endsWith("/") ? contextPath : contextPath + "/";
        return base + "common/libs/ckeditor/";
    }

    @ModelAttribute("sessionExpiry")
    public Long sessionExpiry() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof BaseUser) {
            BaseUser user = (BaseUser) authentication.getPrincipal();
            if ("ADMIN".equals(user.getUserAuthrt())) {
                return adminAccessTokenExpiration;
            }
        }
        return userAccessTokenExpiration;
    }
}
