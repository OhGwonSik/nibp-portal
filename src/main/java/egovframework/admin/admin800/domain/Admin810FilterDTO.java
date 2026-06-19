package egovframework.admin.admin800.domain;

import java.util.Set;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin810FilterDTO {
    private String instOid;     // 기관번호
    private String instNm;     // 기관명
    private String bizRegNo;  //사업자등록번호
    private String repNm;     //대표자명
    private String telNo;     //전화번호
    private String faxNo;     //팩스번호
    private String zipCd;     //우편번호
    private String addr;      //주소
    private String addrDtl;   //상세주소
    private String useYn;     // 사용여부
    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "org_no", "inst_nm", "reg_dt"
    );
    private static final String DEFAULT_SORT = "org_no ASC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
