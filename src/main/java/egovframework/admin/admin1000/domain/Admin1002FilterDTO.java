package egovframework.admin.admin1000.domain;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

/**
 * @ClassName : Admin1002FilterDTO.java
 * @Description : 구성원 검색 필터 DTO
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1002FilterDTO {
    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "indct_seq", "dm.indct_seq", "user_nm", "dm.user_nm",
        "reg_dt", "dm.reg_dt"
    );
    private static final String DEFAULT_SORT = "indct_seq";

    private String sortBy = DEFAULT_SORT;

    private Long deptOid;           // 부서 ID (필수)
    private String userNm;          // 사용자명 검색
    private String jbgd;            // 직책 검색
    private String chrgJob;         // 담당업무

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
