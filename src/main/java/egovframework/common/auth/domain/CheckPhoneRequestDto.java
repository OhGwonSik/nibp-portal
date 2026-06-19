package egovframework.common.auth.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
public class CheckPhoneRequestDto {
    @NotBlank
    private String mpnoPfx;
    @NotBlank
    private String mpnoMid;
    @NotBlank
    private String mpnoSfx;
    private Integer userOid;
}
