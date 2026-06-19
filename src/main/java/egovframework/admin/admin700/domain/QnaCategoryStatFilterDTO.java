package egovframework.admin.admin700.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QnaCategoryStatFilterDTO {

	// admin707
	
    private String searchDate; // 검색 날짜 (YYYY-MM)
    private String searchType; // 월별통계(MONTH) or 연별통계(YEAR)
    
    private String pageId;
    private String reason;
}
