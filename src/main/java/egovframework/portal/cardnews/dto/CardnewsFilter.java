package egovframework.portal.cardnews.dto;

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
public class CardnewsFilter {
    private String keyword;
    private String searchType;

    private Integer page;
    private Integer size;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "cn_no", "a.cn_no", "cn_nm", "a.cn_nm", "reg_dt", "a.reg_dt", "hit_cnt", "a.hit_cnt"
    );
    private static final String DEFAULT_SORT = "card_news_oid DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
