package egovframework.portal.popupzone.dto;

import java.util.Set;

import egovframework.common.util.SortByValidator;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class PopupZoneFilter {
    private String keyword;
    private String searchType;

    private Integer page;
    private Integer size;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "popup_zone_oid", "a.popup_zone_oid", "popup_zone_nm", "a.popup_zone_nm",
        "reg_dt", "a.reg_dt", "hit_cnt", "a.hit_cnt"
    );
    private static final String DEFAULT_SORT = "popup_zone_oid DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
