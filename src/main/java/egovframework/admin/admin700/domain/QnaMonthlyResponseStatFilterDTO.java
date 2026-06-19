package egovframework.admin.admin700.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QnaMonthlyResponseStatFilterDTO {

    private String startYearMonth; // 검색 시작 연월 "2025-01"
    
    private String endYearMonth; // 검색 종료 연월 "2026-01"
    
    private String pageId;
    private String reason;
}
