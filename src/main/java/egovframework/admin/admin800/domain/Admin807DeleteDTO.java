package egovframework.admin.admin800.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
public class Admin807DeleteDTO {
	
	@NotNull(message = "게시판 번호는 필수입니다.")
	private Long bbsOid;
    @Size(max = 1, message = "사용여부는 1자 이하로 입력해주세요.")
	private String useYn;
    private Long menuOid;
	
	private String regId;
	private String mdfcnId;
}
