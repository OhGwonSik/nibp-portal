package egovframework.admin.admin800.domain;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Code { // Admin802VO to Code
	private Integer cdOid; // 코드번호
	@NotNull(message = "그룹코드번호는 필수 입력값입니다.")
	private Integer grpCdOid; // 그룹코드번호
	@NotBlank(message = "코드값은 필수 입력값입니다.")
	@Size(max = 50, message = "코드값은 50자 이하로 입력해주세요.")
	private String cdVal; // 코드값
	@NotBlank(message = "코드명은 필수 입력값입니다.")
	@Size(max = 200, message = "코드명은 200자 이하로 입력해주세요.")
	private String cdNm; // 코드명
	@Size(max = 50, message = "상위코드는 50자 이하로 입력해주세요.")
	private String upCdVal; // 상위코드
	private String cdExpln; // 코드설명
	private Integer cdSeq; // 정렬순서
	private String useYn; // 사용여부
	@Size(max = 200, message = "속성1은 200자 이하로 입력해주세요.")
	private String atrb1; // 속성1
	@Size(max = 200, message = "속성2은 200자 이하로 입력해주세요.")
	private String atrb2; // 속성2
	@Size(max = 200, message = "속성3은 200자 이하로 입력해주세요.")
	private String atrb3; // 속성3
	@Size(max = 200, message = "속성4은 200자 이하로 입력해주세요.")
	private String atrb4; // 속성4
	@Size(max = 200, message = "속성5은 200자 이하로 입력해주세요.")
	private String atrb5; // 속성5
	private String regId; // 등록자ID
	private LocalDateTime regDt; // 등록일시
	private String mdfcnId; // 수정자ID
	private LocalDateTime mdfcnDt; // 수정일시
}
