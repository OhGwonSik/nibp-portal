package egovframework.portal.faq.dto;

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
public class FaqFilter {
    private String keyword;
    private Long faqCtgryOid;
    private String ctgry;

    private Integer page;
    private Integer size;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "faq_no", "tfd.faq_no", "sort_ord", "tfd.sort_ord",
        "reg_dt", "tfd.reg_dt", "view_cnt", "tfd.view_cnt"
    );
    private static final String DEFAULT_SORT = "faq_dtl_oid DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
