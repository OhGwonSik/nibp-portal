package egovframework.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import egovframework.common.interceptor.AccessLogInterceptor;
import egovframework.common.interceptor.FileAccessLogInterceptor;
import egovframework.common.interceptor.RateLimitingInterceptor;
import egovframework.common.security.SecurityConstants;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final RateLimitingInterceptor rateLimitingInterceptor; // 반복 호출 감지
    private final AccessLogInterceptor accessLogInterceptor; // 접근 로그
    private final FileAccessLogInterceptor fileAccessLogInterceptor; // 파일 접근 로그

    @Value("${file.dir}")
    private String fileDir;

    @Value("${file.temp-dir}")
    private String tempFileDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // RateLimitingInterceptor 등록
        // api
        // registry.addInterceptor(rateLimitingInterceptor)
        //         .addPathPatterns("/api/**")  // API 요청에만 적용
        //         .excludePathPatterns(SecurityConstants.RATE_LIMIT_EXCLUDED_PATTERNS) // Rate Limiting 제외 패턴
        //         .excludePathPatterns(
        //                 "/resources/**",
        //                 "/static/**"
        //         ).excludePathPatterns(SecurityConstants.ALL_RESOURCES)
        // ;

        // AccessLogInterceptor 등록
        // page
        registry.addInterceptor(accessLogInterceptor)
                .addPathPatterns("/**") // 모든 요청에 대해 인터셉터 적용
                .excludePathPatterns(SecurityConstants.ALL_RESOURCES)
                .excludePathPatterns("/.well-known/appspecific/com.chrome.devtools.json") // Chrome 개발자 도구 설정파일
                .excludePathPatterns("/favicon.ico")
                .excludePathPatterns(
                        "/resources/**",
                        "/static/**",
                        "/error" // 에러 페이지 제외
                );
        
        // FileAccessLogInterceptor 등록
        registry.addInterceptor(fileAccessLogInterceptor)
                .addPathPatterns("/files/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + fileDir + "/")
                .setCachePeriod(60 * 60);

        registry.addResourceHandler("/temp/**")
                .addResourceLocations("file:" + tempFileDir + "/")
                .setCachePeriod(60 * 60);
    }
}