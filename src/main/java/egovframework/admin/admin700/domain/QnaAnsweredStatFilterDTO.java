package egovframework.admin.admin700.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QnaAnsweredStatFilterDTO {

	// admin705DTO
	
    private String startDate; // from 날짜
    private String endDate; // to 날짜
    private String searchType; // 월별통계(MONTH) or 연별통계(YEAR)
    
    private String pageId;
    private String reason;
    
}
