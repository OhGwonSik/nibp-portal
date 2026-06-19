package egovframework.admin.admin700.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QnaAvgTimeStatDTO {

	//admin706
	private String searchDate;  // 구분 (YYYY-MM 또는 YYYY)
    private double avgDays;     // 평균 소요일 (소수점 포함)
}
