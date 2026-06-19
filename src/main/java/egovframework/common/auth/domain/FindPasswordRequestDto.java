package egovframework.common.auth.domain;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FindPasswordRequestDto {
    @NotBlank(message = "아이디를 입력해 주세요.")
    private String userId;

    @NotBlank(message = "성명을 입력해 주세요.")
    private String userName;

    @NotBlank(message = "이메일 아이디를 입력해 주세요.")
    private String emlLcal;

    @NotBlank(message = "이메일 도메인을 입력해 주세요.")
    private String emlDmn;

    @NotBlank(message = "휴대폰 앞자리를 입력해 주세요.")
    private String mpnoPfx;

    @NotBlank(message = "휴대폰 중간자리를 입력해 주세요.")
    private String mpnoMid;

    @NotBlank(message = "휴대폰 뒷자리를 입력해 주세요.")
    private String mpnoSfx;
}
