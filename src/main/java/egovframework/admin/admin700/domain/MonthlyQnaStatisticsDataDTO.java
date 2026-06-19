package egovframework.admin.admin700.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MonthlyQnaStatisticsDataDTO {
    private String monthGrp; // 월
    private String questionCount; // 질의 등록 건수
    private String draftCount; // 질의응답(안) 작성 건수
    private String finalCount; // 최종 질의응답 작성 건수
    private String draftAverageDate; // 질의응답(안) 평균 작성 소요일
    private String finalAverageDate; // 최종 질의응답 평균 작성 소요일
    private String periodType; // 분류
}
