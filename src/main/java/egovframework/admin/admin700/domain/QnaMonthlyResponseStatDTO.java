package egovframework.admin.admin700.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QnaMonthlyResponseStatDTO {

	private String searchMonth;         // 구분 (Format: YYYY-MM 또는 "전체")

    /* 질의응답(안) - TEMP 기준 */
    private int draftCnt;               // 작성 건수
    private double draftSumDays;        // 작성 소요일 합계 (소수점 2자리)
    private double draftAvgDays;        // 평균 작성 소요일 (소수점 2자리)

    /* 최종 질의응답 - COMP 기준 */
    private int finalCnt;               // 작성 건수
    private double finalSumDays;        // 작성 소요일 합계 (소수점 2자리)
    private double finalAvgDays;        // 평균 작성 소요일 (소수점 2자리)
}
