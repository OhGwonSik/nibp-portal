package egovframework.common.auth.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;

@Getter
@Setter
@ToString
public class CheckIdRequestDto {
    @NotBlank
    private String userId;
    private String userOid;
}
