package egovframework.portal.notice.dto;

import java.util.Set;

import egovframework.common.util.SortByValidator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeFilter {
    private String keyword;
    private String searchType;

    private Integer page;
    private Integer size;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "notice_no", "notice_nm", "reg_dt", "view_cnt"
    );
    private static final String DEFAULT_SORT = "ntc_oid DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
