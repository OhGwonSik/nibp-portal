package egovframework.common.auth.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springmodules.validation.bean.conf.loader.annotation.handler.RegExp;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
public class CheckEmailRequestDto {
    @NotBlank
    private String emlLcal;
    @NotBlank
    @RegExp(value = "/^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$/") // 도메인 정규식
    private String emlDmn;
    private Integer userOid;
}
