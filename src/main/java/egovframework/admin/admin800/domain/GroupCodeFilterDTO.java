package egovframework.admin.admin800.domain;

import java.util.Set;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GroupCodeFilterDTO {
    private String systemId;
    private String systemName;
    private String keyword; // 검색어
    private String useYn; // 사용여부
    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "grp_cd_oid", "system_id", "system_name", "reg_dt"
    );
    private static final String DEFAULT_SORT = "grp_cd_oid ASC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
