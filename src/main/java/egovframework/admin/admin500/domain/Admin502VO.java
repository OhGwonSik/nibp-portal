package egovframework.admin.admin500.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin502VO {

    /* 설문 정보 */
    private Long srvyOid;
    private String srvyTtl;
    private String srvyBgngDt;
    private String srvyEndDt;

    /* 설문 응답 집계 */
    private Integer surveyCmplCnt;   // 응답 완료 수

    /* 상태값 (조사중 / 조사종료 / 조사전) */
    private String surveyStatus;
	
}
