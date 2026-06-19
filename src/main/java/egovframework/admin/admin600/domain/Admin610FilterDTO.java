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
public class Admin610FilterDTO {
    private String keyword;        // 검색어
    private String ansYn;       // 답변 완료 여부
    private LocalDateTime regDt;   // 등록일시
    private Long qnaOid;            // QnA ID
    private Long upQnaOid;         // 부모 QnA ID
    private String useYn;          // 사용 여부
    private String qnaTtl;          // 제목
    private String ctgry;       // 분류

    private LocalDate regDtSt;     // 등록일 시작
    private LocalDate regDtEd;     // 등록일 끝

    private Integer page = 1;
    private Integer size = 10;

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "q.qna_oid", "q.reg_dt", "q.inq_cnt", "q.ans_yn",
        "a.reg_dt"
    );
    private static final String DEFAULT_SORT = "q.reg_dt DESC, a.reg_dt";

    private String sortBy = DEFAULT_SORT;

    public void setSortBy(String sortBy) {
        this.sortBy = SortByValidator.sanitize(sortBy, ALLOWED_COLUMNS, DEFAULT_SORT);
    }
}
