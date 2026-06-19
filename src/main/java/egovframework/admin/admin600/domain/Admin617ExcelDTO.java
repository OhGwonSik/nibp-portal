package egovframework.admin.admin600.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class Admin617ExcelDTO {
    private Long cardNewsOid;                 // 카드뉴스 ID
    private String cardNewsNm;               // 제목
    private LocalDate regDt;           // 등록일시
    private String openYn;             // 공개 여부
    private String openState;          // 공개 여부 텍스트
    private Integer inqCnt;            // 조회수
}