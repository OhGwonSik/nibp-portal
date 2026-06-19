package egovframework.common.auth.domain;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FindIdRequestDto {
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String userName;

    @NotBlank(message = "이메일 아이디를 입력해주세요.")
    private String emlLocal;

    @NotBlank(message = "이메일 도메인을 입력해주세요.")
    private String emlDmn;

    @NotBlank(message = "휴대폰 앞자리를 입력해주세요.")
    private String mpnoPfx;

    @NotBlank(message = "휴대폰 중간 번호를 입력해주세요.")
    private String mpnoMid;

    @NotBlank(message = "휴대폰 끝 번호를 입력해주세요.")
    private String mpnoSfx;

    public String getEmail() { return emlLocal + "@" + emlDmn; }

    public String getPhoneNumber() { return mpnoPfx + mpnoMid + mpnoSfx; }
}
