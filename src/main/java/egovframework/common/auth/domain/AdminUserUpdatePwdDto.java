package egovframework.common.auth.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class AdminUserUpdatePwdDto {
    @NotBlank
    // @Pattern(regexp = "^(?=(.*[a-z]){1,})(?=(.*[A-Z]){1,})(?=(.*\\d){1,})|(?=(.*[a-z]){1,})(?=(.*[A-Z]){1,})(?=(.*[@$!%*?&]){1,})|(?=(.*[a-z]){1,})(?=(.*\\d){1,})(?=(.*[@$!%*?&]){1,})|(?=(.*[A-Z]){1,})(?=(.*\\d){1,})(?=(.*[@$!%*?&]){1,})[A-Za-z\\d@$!%*?&]{8,}$", message = "비밀번호는 8자 이상의 영문대문자, 영문소문자, 특수기호, 숫자 중 3가지 이상 조합이어야 합니다.")
    private String currentPswd;
    @NotBlank
    @Pattern(regexp = "^(?=(.*[a-z]){1,})(?=(.*[A-Z]){1,})(?=(.*\\d){1,})|(?=(.*[a-z]){1,})(?=(.*[A-Z]){1,})(?=(.*[@$!%*?&]){1,})|(?=(.*[a-z]){1,})(?=(.*\\d){1,})(?=(.*[@$!%*?&]){1,})|(?=(.*[A-Z]){1,})(?=(.*\\d){1,})(?=(.*[@$!%*?&]){1,})[A-Za-z\\d@$!%*?&]{8,}$", message = "비밀번호는 8자 이상의 영문대문자, 영문소문자, 특수기호, 숫자 중 3가지 이상 조합이어야 합니다.")
    private String newPswd;
    @NotBlank
    @Pattern(regexp = "^(?=(.*[a-z]){1,})(?=(.*[A-Z]){1,})(?=(.*\\d){1,})|(?=(.*[a-z]){1,})(?=(.*[A-Z]){1,})(?=(.*[@$!%*?&]){1,})|(?=(.*[a-z]){1,})(?=(.*\\d){1,})(?=(.*[@$!%*?&]){1,})|(?=(.*[A-Z]){1,})(?=(.*\\d){1,})(?=(.*[@$!%*?&]){1,})[A-Za-z\\d@$!%*?&]{8,}$", message = "비밀번호는 8자 이상의 영문대문자, 영문소문자, 특수기호, 숫자 중 3가지 이상 조합이어야 합니다.")
    private String newPwdConfirm;
    private LocalDateTime mdfcnDt;
}
