package egovframework.admin.admin600.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class Admin606CategoryVO {
    private Long faqCtgryOid;                            // 카테고리 ID
    private String ctgryNm;                          // 카테고리 명
    private Integer sortSeq;                        // 정렬 순서
    private String useYn;                           // 사용 여부
    private String regId;                       // 등록자 ID
    private LocalDate regDt;                        // 등록일시
    private String mdfcnId;                       // 수정자 ID
    private LocalDate mdfcnDt;                        // 수정일시
}
