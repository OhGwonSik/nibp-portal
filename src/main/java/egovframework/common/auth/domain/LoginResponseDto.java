package egovframework.common.auth.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class LoginResponseDto {
    private BaseUserDto user;
    @ToString.Exclude
    private String accessToken;
    @ToString.Exclude
    private String refreshToken;
    private long expiresIn;
    private Boolean secondAuthRequired; // 2차 인증 필요 여부
    private String popupUrl;            // 본인인증 팝업 URL
}
