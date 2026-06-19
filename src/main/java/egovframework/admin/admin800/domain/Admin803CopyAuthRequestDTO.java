package egovframework.admin.admin800.domain;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin803CopyAuthRequestDTO {
	@NotNull(message = "소스 사용자번호는 필수입니다.")
	private Long sourceUserNo;

	@NotEmpty(message = "타겟 사용자번호 목록은 필수입니다.")
	private List<Long> targetUserNos;

	@NotBlank(message = "변경 사유는 필수 입력값입니다.")
	private String changeReason;
}
