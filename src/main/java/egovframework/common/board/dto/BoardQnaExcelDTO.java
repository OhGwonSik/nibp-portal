package egovframework.common.board.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoardQnaExcelDTO {

	private Long bbsPstOid;
	private String bbsNm;                // 게시구분 (예: 생명윤리법QNA)
    private String ctgry;                // 문의구분 (카테고리)
    private String bbsPstTtl;            // 제목
    private String satisfaction;         // 만족도
    private String satisfactionDate;     // 만족도 평가일
    private String questionDate;    // 질문일 (reg_dt)
    private String draftDate;       // 초안작성일 (히스토리 TEMP 최초 시점)
    
    private String answererId;           // 답변자 (답변 게시글 등록자 ID)
    private String finalAnswerDate; // 최종답변일 (히스토리 COMP 최초 시점)

    /**
     * 초안 소요일 (초일불산입 + 휴일제외)
     * 소수점 2자리까지 계산된 값 (예: 1.25일)
     */
    private Double draftResponseWorkDays;

    /**
     * 최종 소요일 (초일불산입 + 휴일제외)
     * 소수점 2자리까지 계산된 값 (예: 3.50일)
     */
    private Double finalResponseWorkDays;

    private String questionContent;      // 질문내용
    private String answerContent;        // 답변내용
}
