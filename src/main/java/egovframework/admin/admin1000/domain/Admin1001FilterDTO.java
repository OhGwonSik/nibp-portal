package egovframework.admin.admin1000.domain;

import java.util.Set;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : Admin1001FilterDTO.java
 * @Description : 부서 검색 필터 DTO
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1001FilterDTO {
    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "sort_seq", "d.sort_seq", "dept_nm", "d.dept_nm", "reg_dt", "d.reg_dt"
    );
    private static final String DEFAULT_SORT = "sort_seq";

    private String sortBy = DEFAULT_SORT;

    private String deptNm;       // 부서명 검색
    private Long upDeptOid;         // 상위 부서 검색
    private String useYn;          // 사용 여부

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
