package egovframework.common.auth.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class NicePassVerifyCallbackDto {
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String userId;

    @NotNull(message = "NICE PASS 인증 결과(이름)는 필수 입력 값입니다.")
    private String name;

    @NotNull(message = "NICE PASS 인증 결과(생년월일)는 필수 입력 값입니다.")
    private String brdt;

    @NotNull(message = "NICE PASS 인증 결과(전화번호)는 필수 입력 값입니다.")
    private String mpnoPfx;

    @NotNull(message = "NICE PASS 인증 결과(전화번호)는 필수 입력 값입니다.")
    @ToString.Exclude
    private String mpnoMid;

    @NotNull(message = "NICE PASS 인증 결과(전화번호)는 필수 입력 값입니다.")
    private String mpnoSfx;
}
