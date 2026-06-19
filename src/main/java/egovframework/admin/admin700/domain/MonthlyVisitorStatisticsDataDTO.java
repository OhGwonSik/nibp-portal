package egovframework.admin.admin700.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MonthlyVisitorStatisticsDataDTO {
    private String monthGrp; // 월
    private String visitor; // 방문자수(UV)
    private String dailyVisitorAverage; // 일평균
    private String periodType; // 분류
}
