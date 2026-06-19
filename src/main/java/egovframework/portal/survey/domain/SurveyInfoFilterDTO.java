package egovframework.portal.survey.domain;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
public class SurveyInfoFilterDTO {
	private Long srvyOid;
    private String keyword;
    private String searchType;

    private Integer page;
    private Integer size;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "srvy_oid", "ts.srvy_oid", "srvy_ttl", "ts.srvy_ttl",
        "srvy_bgng_dt", "ts.srvy_bgng_dt", "srvy_end_dt", "ts.srvy_end_dt"
    );
    private static final String DEFAULT_SORT = "srvy_oid DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
