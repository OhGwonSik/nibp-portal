package egovframework.portal.survey.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AnswerDTO {
	private Long srvyQitemOid;    // 문항번호 (필수)
    private Long srvyQitemOptOid;    // 보기번호 (객관식, 비율, 순위형 필수 / 주관식 null)
    private String ansVal; // 답변값 (주관식 내용, 등수, 점수, 기타의견 등 모두 String 처리)
    private String type;

    private Long fileOid;
}
