package egovframework.portal.faq.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FaqCategoryDTO {

    private Long faqCtgryOid; // 카테고리 ID
    private String ctgryNm; // 카테고리 명
    private Integer sortSeq; // 정렬 순서
    private String useYn; // 사용여부 (Y: 사용, N: 미사용)
    private String regId; // 등록자 ID
    private LocalDateTime regDt; // 등록일시
    private String mdfcnId; // 수정자 ID
    private LocalDateTime mdfcnDt; // 수정일시
}
