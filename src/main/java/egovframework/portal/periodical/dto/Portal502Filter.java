package egovframework.portal.periodical.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : Portal502Filter.java
 * @Description : 정기발간자료 목록 조회 Filter DTO
 *
 * @author : j.h.kim
 * @since : 2025. 01. 13
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Portal502Filter {
    private String searchType;      // 검색 타입 (bbsall, bbstitle, bbsconts)
    private String searchKeyword;   // 검색 키워드
    private Integer pageNum = 1;    // 페이지 번호
    private Integer pageCnt = 6;   // 페이지 사이즈 (목록에서 6개씩 표시)
}
