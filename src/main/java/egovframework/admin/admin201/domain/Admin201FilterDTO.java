package egovframework.admin.admin201.domain;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
public class Admin201FilterDTO {

	private Long pblDataOpnnOid;
	private String keyword;          // 검색어 (제목)
    private String regDtFrom;        // 검색 시작일 (YYYY-MM-DD)
    private String regDtTo;          // 검색 종료일 (YYYY-MM-DD)

    private int pageIndex = 1;       // 현재 페이지 번호 (기본값 1)
    private int pageSize = 10;       // 페이지당 출력 개수 (기본값 10)

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "pbl_data_opnn_oid", "reg_dt", "pbl_data_opnn_ttl"
    );
    private static final String DEFAULT_SORT = "pbl_data_opnn_oid DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
