package egovframework.admin.admin500.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
public class Admin502ExcelDTO {
	@NotNull(message = "설문 번호(srvyOid)는 필수 입력 값입니다.")
    private Long srvyOid;

    @NotNull(message = "사유는 필수 입력 값입니다.")
    @Size(min = 2, message = "사유는 2자 이상 입력해주세요.")
    private String reason;
}