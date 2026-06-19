package egovframework.portal.faq.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FaqDTO {

    private Long faqDtlOid; // FAQ ID
    private Long faqCtgryOid; // 카테고리 ID (FK)
    private String qstnCn; // 질문 내용
    private String ansCn; // 답변 내용 (CKEditor 등 HTML 저장)
    private Integer sortSeq; // 정렬 순서
    private String openYn; // 공개여부 (Y: 공개, N: 비공개)
    private String useYn; // 사용여부 (Y: 사용, N: 미사용)
    private Integer inqCnt; // 조회수
    private String regId; // 등록자 ID
    private LocalDateTime regDt; // 등록일시
    private String mdfcnId; // 수정자 ID
    private LocalDateTime mdfcnDt; // 수정일시
}