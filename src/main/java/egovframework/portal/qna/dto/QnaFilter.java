package egovframework.portal.qna.dto;

import java.util.Set;

import egovframework.common.util.SortByValidator;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class QnaFilter {
    private String keyword;
    private String searchType;
    private String ctgry;

    private Integer page;
    private Integer size;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "qna_no", "title", "reg_dt", "view_cnt", "answer_yn"
    );
    private static final String DEFAULT_SORT = "qna_oid DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
