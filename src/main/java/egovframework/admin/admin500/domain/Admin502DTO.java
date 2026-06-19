package egovframework.admin.admin500.domain;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin502DTO {
	@NotNull(message = "설문 번호(srvyOid)는 필수 입력 값입니다.")
    private Long srvyOid;
}