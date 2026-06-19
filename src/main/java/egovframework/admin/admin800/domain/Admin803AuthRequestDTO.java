package egovframework.admin.admin800.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@ToString
public class Admin803AuthRequestDTO {
    @NotEmpty(message = "권한 목록은 필수입니다.")
    private List<Admin803AuthDTO> authList;

    @NotBlank(message = "변경 사유는 필수 입력값입니다.")
    private String changeReason;
}