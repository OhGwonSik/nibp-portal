package egovframework.admin.admin800.domain;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
public class Admin807FilterDTO {

	private Integer bbsOid;
	private String bbsSeCd;
	private String useYn;
	private String regStartDt;
	private String regEndDt;

	private String srchKywd;
	private String searchCondition;

	private String menuAuthLv;

	// 페이징
    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "bbs_oid", "tb.bbs_oid", "bbs_nm", "tb.bbs_nm",
        "bbs_se_cd", "tb.bbs_se_cd", "reg_dt", "tb.reg_dt"
    );
    private static final String DEFAULT_SORT = "bbs_oid DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
