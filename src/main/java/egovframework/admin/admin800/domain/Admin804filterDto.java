package egovframework.admin.admin800.domain;

import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import egovframework.common.util.SortByValidator;
import groovy.transform.ToString;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ToString
public class Admin804filterDto {
    private String searchType;
    private String searchValue;
    private String startDate;
    private String endDate;
    private String userId;
    private String ipAddr;
    private String acsType;
    private String rqtUri;

    private String reason;

    @NotBlank(message = "연월 정보는 필수입니다.")
    @Pattern(regexp = "^\\d{4}-?\\d{2}$", message = "연월 형식이 올바르지 않습니다. (YYYY-MM 또는 YYYYMM)")
    private String excelExportYm;

    private Integer page;
    private Integer size;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "user_acs_log_oid", "user_id", "acs_type", "reg_dt", "ip_addr", "stts_cd"
    );
    private static final String DEFAULT_SORT = "reg_dt DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
