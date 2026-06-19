package egovframework.admin.admin1100.domain;

import java.time.LocalDate;
import java.util.Set;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : Admin1101FilterDTO.java
 * @Description : 정기발간자료 목록 검색 조건 DTO
 *
 * @author : j.h.kim
 * @since : 2025. 01. 12
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1101FilterDTO {
    private String searchKeyword;       // 검색어 (제목)
    private String openYn;              // 공개상태 (Y/N/전체)
    private LocalDate bgngDt;          // 기간 검색 시작일
    private LocalDate endDt;            // 기간 검색 종료일

    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "p.fxtm_pbls_data_oid", "p.fxtm_pbls_data_ttl", "p.pblcn_dt", "p.sort_seq",
        "p.reg_dt", "p.inq_cnt"
    );
    private static final String DEFAULT_SORT = "p.pblcn_dt DESC, p.fxtm_pbls_data_oid DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
