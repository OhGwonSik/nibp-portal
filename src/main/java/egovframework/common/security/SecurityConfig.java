package egovframework.common.security;

import egovframework.common.auth.service.AuthService;
import egovframework.common.auth.service.CustomUserDetailsService;
import egovframework.common.auth.service.RefreshTokenService;
import egovframework.common.auth.service.SessionService;
import egovframework.common.jwt.JwtAuthenticationEntryPoint;
import egovframework.common.jwt.JwtAuthenticationFilter;
import egovframework.common.jwt.JwtTokenProvider;
import egovframework.common.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.util.unit.DataSize;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.MultipartConfigElement;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    // 보안 헤더 설정 (application.yml에서 읽음)
    @Value("${app.security.hsts.enabled:false}")
    private boolean hstsEnabled;

    @Value("${app.security.hsts.max-age:31536000}")
    private long hstsMaxAge;

    @Value("${app.security.hsts.include-subdomains:true}")
    private boolean hstsIncludeSubdomains;

    @Value("${app.security.frame-options.enabled:true}")
    private boolean frameOptionsEnabled;

    @Value("${app.security.frame-options.mode:SAMEORIGIN}")
    private String frameOptionsMode;

    @Value("${app.security.content-type-options.enabled:true}")
    private boolean contentTypeOptionsEnabled;

    @Value("${app.security.xss-protection.enabled:true}")
    private boolean xssProtectionEnabled;

    @Value("${app.security.xss-protection.mode:block}")
    private String xssProtectionMode;

    // HTTPS 리다이렉트 설정
    @Value("${app.security.https-redirect.enabled:false}")
    private boolean httpsRedirectEnabled;

    // CORS 설정 (application.yml에서 읽음)
    @Value("${app.cors.allowed-origins}")
    private String[] corsAllowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String[] corsAllowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String[] corsAllowedHeaders;

    @Value("${app.cors.exposed-headers:Authorization,Content-Disposition}")
    private String[] corsExposedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean corsAllowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long corsMaxAge;

    // SecurityConstants에서 상수 사용
    private static final String[] PUBLIC_AUTH_URLS = SecurityConstants.PUBLIC_AUTH_URLS;
    private static final String[] USER_API_URLS = SecurityConstants.USER_API_URLS;
    private static final String[] ADMIN_API_URLS = SecurityConstants.ADMIN_API_URLS;
    private static final String[] PUBLIC_API_URLS = SecurityConstants.PUBLIC_API_URLS;
    private static final String[] MAIN_URLS = SecurityConstants.MAIN_URLS;
    private static final String[] PORTAL_RESOURCES = SecurityConstants.PORTAL_RESOURCES;
    private static final String[] ADMIN_RESOURCES = SecurityConstants.ADMIN_RESOURCES;
    private static final String[] COMMON_RESOURCES = SecurityConstants.COMMON_RESOURCES;
    private static final String[] COMMON_AUTH_URLS = SecurityConstants.COMMON_AUTH_URLS;
    private static final String[] BOARD_URLS = SecurityConstants.BOARD_URLS;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin 설정 (application.yml에서 읽음)
        configuration.setAllowedOrigins(Arrays.asList(corsAllowedOrigins));

        // 허용할 HTTP 메소드 설정
        configuration.setAllowedMethods(Arrays.asList(corsAllowedMethods));

        // 허용할 헤더 설정 (보안 강화: 와일드카드 제거)
        configuration.setAllowedHeaders(Arrays.asList(corsAllowedHeaders));

        // 노출할 헤더 설정 (클라이언트에서 읽을 수 있는 헤더)
        configuration.setExposedHeaders(Arrays.asList(corsExposedHeaders));

        // 자격 증명(쿠키 등) 허용 여부
        configuration.setAllowCredentials(corsAllowCredentials);

        // Preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(corsMaxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxRequestSize(DataSize.ofMegabytes(100L));
        factory.setMaxFileSize(DataSize.ofMegabytes(100L));
        return factory.createMultipartConfig();
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http,
                                              CustomUserDetailsService customUserDetailsService,
                                              JwtTokenProvider jwtTokenProvider,
                                              AuthService authService,
                                              RefreshTokenService refreshTokenService,
                                              SessionService sessionService,
                                              CookieUtil cookieUtil) throws Exception {
        http
                .csrf(
                        csrf -> csrf.disable()
                )
                .cors(
                        cors -> cors.configurationSource(corsConfigurationSource())
                )
                .authorizeHttpRequests(
                        authorize -> authorize
                                // 파비콘
                                .antMatchers("/favicon.ico").permitAll()
                                // 인증 API 및 로그인 페이지 허용
                                .antMatchers(PUBLIC_AUTH_URLS).permitAll()
                                // 메인 페이지 허용
                                .antMatchers(HttpMethod.GET, MAIN_URLS).permitAll()
                                // 정적 리소스 허용 (CSS, JS, 이미지 등)
                                .antMatchers(ADMIN_RESOURCES).permitAll() // admin 규칙보다 항상 위에
                                .antMatchers(PORTAL_RESOURCES).permitAll()
                                .antMatchers(COMMON_RESOURCES).permitAll()
                                .antMatchers(PUBLIC_API_URLS).permitAll()
                                .antMatchers(COMMON_AUTH_URLS).permitAll()
                                .antMatchers(BOARD_URLS).permitAll()
                                .antMatchers(USER_API_URLS).authenticated()
                                .antMatchers(ADMIN_API_URLS).hasRole("ADMIN")
                                .antMatchers("/admin/**").hasRole("ADMIN") // 나머지 admin
                                // 나머지는 인증 필요
                                .anyRequest().authenticated()
                )
                .exceptionHandling(
                        handling -> handling.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandler)
                )
                .sessionManagement(
                        management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .headers(headers -> {
                    // X-Content-Type-Options: nosniff (MIME 타입 스니핑 방지)
                    if (contentTypeOptionsEnabled) {
                        headers.contentTypeOptions(contentTypeOptions -> {});
                    }

                    // X-Frame-Options (Clickjacking 방지)
                    if (frameOptionsEnabled) {
                        if ("DENY".equalsIgnoreCase(frameOptionsMode)) {
                            headers.frameOptions(frameOptions -> frameOptions.deny());
                        } else {
                            headers.frameOptions(frameOptions -> frameOptions.sameOrigin());
                        }
                    }

                    // X-XSS-Protection (XSS 공격 방지 - 레거시 브라우저용)
                    if (xssProtectionEnabled) {
                        headers.xssProtection(xss -> xss
                                .xssProtectionEnabled(true)
                                .block("block".equalsIgnoreCase(xssProtectionMode))
                        );
                    }

                    // HSTS (HTTP Strict Transport Security) - HTTPS 강제
                    // 로컬 개발 환경(HTTP)에서는 비활성화, 운영 환경(HTTPS)에서는 활성화
                    if (hstsEnabled) {
                        headers.httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(hstsMaxAge)
                                .includeSubDomains(hstsIncludeSubdomains));
                    }

                    // Referrer-Policy (리퍼러 정보 제어)
                    headers.referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN));

                    // Cache-Control 헤더는 Spring Security 기본값 사용
                    headers.cacheControl(cache -> {});
                })
                .addFilterBefore(new JwtAuthenticationFilter(customUserDetailsService, jwtTokenProvider, authService, refreshTokenService, sessionService, cookieUtil), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new LoginPageRedirectFilter(), JwtAuthenticationFilter.class);

        // HTTP → HTTPS 리다이렉트 필터 (X-Forwarded-Proto 기반)
        if (httpsRedirectEnabled) {
            http.addFilterBefore(new HttpsRedirectFilter(), SecurityContextPersistenceFilter.class);
        }

        return http.build();
    }
}