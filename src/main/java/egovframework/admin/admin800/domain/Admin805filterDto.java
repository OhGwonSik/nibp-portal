package egovframework.admin.admin800.domain;

import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin805filterDto {
    private String searchType;
    private String searchValue;
    private String startDate;
    private String endDate;
    private String chnrgUserId;
    private String trgtUserId;
    private String chgType;
    private String prmsnType;

    private String reason;

    @NotBlank(message = "연월 정보는 필수입니다.")
    @Pattern(regexp = "^\\d{4}-?\\d{2}$", message = "연월 형식이 올바르지 않습니다. (YYYY-MM 또는 YYYYMM)")
    private String excelExportYm;
    
    private Integer page;
    private Integer size;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "pcl.prmsn_chg_log_oid", "pcl.chg_dt", "pcl.chnrg_user_id",
        "pcl.trgt_user_id", "pcl.chg_type", "pcl.reg_dt"
    );
    private static final String DEFAULT_SORT = "chg_dt DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
