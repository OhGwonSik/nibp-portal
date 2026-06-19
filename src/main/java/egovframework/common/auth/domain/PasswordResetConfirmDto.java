package egovframework.common.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
public class PasswordResetConfirmDto {

    @JsonIgnore
    private Long userOid;

    @ToString.Exclude
    @NotBlank(message = "새 비밀번호는 필수 입력 값입니다.")
    private String newPswd;

    @ToString.Exclude
    @NotBlank(message = "새 비밀번호 확인은 필수 입력 값입니다.")
    private String newPwdConfirm;

}
