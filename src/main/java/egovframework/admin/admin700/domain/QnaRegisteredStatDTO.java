package egovframework.admin.admin700.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QnaRegisteredStatDTO {

	// admin704DTO
	
    private String searchDate; // 구분(화면의 날짜) / 월별 통계 시: "YYYY-MM" (예: "2024-05") / 연별 통계 시: "YYYY" (예: "2024")

    private int inquiryCnt; //질의 건수
    
}
