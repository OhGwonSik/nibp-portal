package egovframework.admin.admin800.domain;

import java.util.Set;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CodeFilterDTO {
    private String grpCdOid;
    private String useYn; // 사용여부
    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "cd_seq", "grp_cd_oid", "reg_dt"
    );
    private static final String DEFAULT_SORT = "cd_seq ASC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
