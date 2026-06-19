package egovframework.admin.admin600.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class Admin606VO {
    private Long faqDtlOid;                             // FAQ ID
    private Long faqCtgryOid;                            // 카테고리 ID (FK)
    private String qstnCn;                        // 질문 내용
    private String ansCn;                          // 답변 내용 (HTML 저장)
    private String ansCnTxt;                      // 답변 내용 (태그제거)
    private Integer sortSeq;                        // 정렬 순서
    private String openYn;                          // 공개여부
    private String useYn;                           // 사용여부
    private Integer inqCnt;                        // 조회수
    private String regId;                       // 등록자 ID
    private LocalDate regDt;                        // 등록일시
    private String mdfcnId;                       // 수정자 ID
    private LocalDate mdfcnDt;                        // 수정일시

    private String ctgryNm;                          // 카테고리 명
}
