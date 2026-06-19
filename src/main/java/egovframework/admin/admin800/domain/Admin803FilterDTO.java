package egovframework.admin.admin800.domain;

import java.util.Set;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin803FilterDTO {
    private String keyword;
    private String userOid;
    private String userNmKorn;
    private String userId;
    private String useYn;
    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "user_oid", "user_nm_korn", "user_id"
    );
    private static final String DEFAULT_SORT = "user_oid ASC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
