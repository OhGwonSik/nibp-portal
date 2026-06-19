package egovframework.portal.video.dto;

import egovframework.common.util.SortByValidator;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
@Builder
public class VideoFilter {
    private String keyword;
    private String searchType;

    private Integer page;
    private Integer size;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "video_no", "video_nm", "reg_dt", "hit_cnt"
    );
    private static final String DEFAULT_SORT = "vdo_oid DESC";

    @Builder.Default
    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
