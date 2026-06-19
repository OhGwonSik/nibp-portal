package egovframework.common.board.domain;

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
public class BoardFilter {
    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "bbs_pst_oid", "tbp.bbs_pst_oid", "post_title", "tbp.post_title",
        "reg_dt", "tbp.reg_dt", "view_cnt", "tbp.view_cnt",
        "like_cnt", "tbp.like_cnt", "comment_cnt", "tbp.comment_cnt"
    );
    private static final String DEFAULT_SORT = "bbs_pst_oid DESC";

    @Builder.Default
    private String sortBy = DEFAULT_SORT;
    private String category;  // 카테고리 필터 (국내언론동향/해외언론동향 등)

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
