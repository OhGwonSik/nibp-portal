package egovframework.admin.admin700.domain;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@ToString
public class Admin701FilterDTO {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromQuestionDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toQuestionDate;
    private Integer pageIndex;
    private Integer pageSize;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "tbbp.reg_dt", "tbbp.category", "tbbp.post_title",
        "tbqs.reg_dt", "ans.first_reply_dt"
    );
    private static final String DEFAULT_SORT = "tbbp.reg_dt DESC";

    private String sortBy = DEFAULT_SORT;
    private String reason;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
