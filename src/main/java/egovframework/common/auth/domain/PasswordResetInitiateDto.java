package egovframework.common.auth.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
public class PasswordResetInitiateDto {
    @NotBlank(message = "사용자 ID는 필수 입력 값입니다.")
    private String userId;
}
