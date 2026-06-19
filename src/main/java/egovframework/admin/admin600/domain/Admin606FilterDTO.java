package egovframework.admin.admin600.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import egovframework.common.util.SortByValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin606FilterDTO {
    private Long faqCtgryOid;           // 카테고리 ID
    private String keyword;        // 검색어
    private String openYn;         // 공개여부
    private LocalDateTime regDt;   // 등록일시
    private Long faqDtlOid;            // FAQ ID
    private String qstnCn;       // 질문 내용

    private LocalDate regDtSt;     // 등록일 시작
    private LocalDate regDtEd;     // 등록일 끝

    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "f.faq_dtl_oid", "f.sort_seq", "f.reg_dt", "f.inq_cnt", "f.faq_ctgry_oid"
    );
    private static final String DEFAULT_SORT = "f.sort_seq, f.faq_dtl_oid DESC";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
