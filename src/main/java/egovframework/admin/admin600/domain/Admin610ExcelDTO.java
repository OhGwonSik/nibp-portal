package egovframework.admin.admin600.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Q&A 엑셀 다운로드용 DTO
 */
@Getter
@Setter
@ToString
public class Admin610ExcelDTO {
    private Long qnaOid;                   // 번호
    private String ctgry;              // 분류
    private String qnaTtl;                 // 제목
    private LocalDateTime regDt;          // 등록일시
    private LocalDateTime answerRegDt;    // 답변일시
    private String ansYn;              // 답변여부
    private String answerWriterNm;        // 답변자
    private String secretYn;              // 공개여부
    private String secretYnStr;           // 공개여부 텍스트
    private Integer inqCnt;              // 조회수
}

