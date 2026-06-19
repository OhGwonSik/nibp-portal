package egovframework.admin.admin600.domain;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@ToString
public class Admin618FilterDTO {
    private String keyword;            // 검색어
    private Long popupZoneOid;                 // 팝업존 ID
    private String popupZoneNm;               // 팝업존 제목
    private String useYn;              // 사용 여부
    private String openYn;             // 공개 여부

    private LocalDate regDt;           // 등록일시
    private LocalDate regDtSt;         // 등록일시 시작일
    private LocalDate regDtEd;         // 등록일시 종료일

    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "popup_zone_oid", "popup_zone_nm", "reg_dt", "hit_cnt"
    );
    private static final String DEFAULT_SORT = "popup_zone_oid DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
