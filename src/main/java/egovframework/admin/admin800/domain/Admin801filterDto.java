package egovframework.admin.admin800.domain;

import java.util.Set;

import egovframework.common.util.SortByValidator;
import groovy.transform.ToString;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ToString
public class Admin801filterDto {
    private String keyword; // VARCHAR(255) 사용자명 및 ID
    private String emlLcal; // VARCHAR(64) 이메일(로컬)
    private String emlDmn; // VARCHAR(255) 이메일(도메인)
    private String emlInpTyp; // ENUM('직접입력', '선택') 이메일 입력 타입
    private String mpno; // VARCHAR(15) 휴대폰번호
    private String instNm; // VARCHAR(100) 조직명
    private String regDtFrom; // TIMESTAMP 생성일자 From
    private String regDtTo; // TIMESTAMP 생성일자 To
    private String useYn; // VARCHAR(1) 사용여부
    private String prvcUseYn; // VARCHAR(1) 개인정보취급여부

    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "user_oid", "user_id", "user_nm_korn", "org_nm", "reg_dt"
    );
    private static final String DEFAULT_SORT = "reg_dt DESC, user_oid ASC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
