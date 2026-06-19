package egovframework.admin.admin600.domain;

import java.time.LocalDate;
import java.util.Set;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin614FilterDTO {
    private String keyword;           // 검색어
    private Long popupOid;             // 팝업 ID
    private String popupTtl;           // 팝업 제목
    private String useYn;             // 사용 여부 (Y: 사용, N: 미사용)
    private LocalDate popupBgngDt;        // 게시 시작 일시
    private LocalDate popupEndDt;          // 게시 종료 일시 (NULL이면 무기한)

    private String openState;         // 공개상태(대기, 게재, 게재 종료)

    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "popup_oid", "popup_ttl", "popup_bgng_dt", "popup_end_dt", "reg_dt"
    );
    private static final String DEFAULT_SORT = "popup_oid DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
