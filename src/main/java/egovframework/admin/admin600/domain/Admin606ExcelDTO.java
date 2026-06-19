package egovframework.admin.admin600.domain;

import java.time.LocalDate;

import groovy.transform.ToString;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ToString
public class Admin606ExcelDTO {
    private Long faqDtlOid;                             // FAQ ID
    private String ctgryNm;                          // 카테고리 명
    private String qstnCn;                        // 질문 내용
    private String openYn;                          // 공개여부
    private String useYn;                           // 사용여부
    private LocalDate regDt;                        // 등록일시
    private Integer inqCnt;                        // 조회수   
}