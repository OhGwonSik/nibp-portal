package egovframework.common.board.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class BoardPostSatisfactionDTO {
	@NotNull(message = "게시글 번호(bbsPstOid)는 필수 입력 값입니다.")
	private Long bbsPstOid;
	
    // qna 만족도 평가
    private String tblNm; // 대상 테이블명
    private Long tblOid; // 대상 테이블 PK
    private String regId; // 등록자(답글자) ID
    private String stts;
    private String chc;
    private String updId;
}
