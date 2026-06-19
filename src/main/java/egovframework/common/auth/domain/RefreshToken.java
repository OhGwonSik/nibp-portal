package egovframework.common.auth.domain;

import lombok.*;

/**
 * @ClassName : RefreshToken.java
 * @Description : Refresh Token 도메인
 *
 * @author : tspark
 * @since  : 2025. 11. 04
 * @version : 1.0
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    private String tokenId;      // 토큰 고유 ID (PK)
    private String userId;       // 사용자 ID
    @ToString.Exclude
    private String token;        // Refresh Token 값
    private String expiryDt;     // 만료 일시
    private String regDt;        // 생성 일시
    private String lastUsedDt;   // 마지막 사용 일시
    private String revokedYn;    // 폐기 여부 (Y/N)
    private String revokedDt;    // 폐기 일시
}
