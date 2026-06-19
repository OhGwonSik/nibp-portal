package egovframework.common.auth.domain;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Redis 세션 관리용 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {
    private String userId;            // 사용자 ID
    private String userRole;          // 사용자 역할 (USER, ADMIN)
    private String loginSessionId;    // 로그인 세션 ID
    @ToString.Exclude
    private String accessTokenHash;   // Access Token 해시값
    private String refreshTokenId;    // Refresh Token ID (jti)
    private LocalDateTime loginAt;    // 로그인 일시
    private String ipAddress;         // 접속 IP
    private String userAgent;         // 브라우저 정보
}
