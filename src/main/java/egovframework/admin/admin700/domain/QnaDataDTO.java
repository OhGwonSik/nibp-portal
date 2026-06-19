package egovframework.admin.admin700.domain;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QnaDataDTO {
    private int qnaOrderNo;
    private String ctgry;
    private String isSatisfied;
    private LocalDate satisfactionDate;
    private String bbsPstTtl;
    private LocalDate answerTempDate;
    private LocalDate questionDate;
    private LocalDate ansCnDate;
    private String draftResponseWorkDays;
    private String finalResponseWorkDays;
}
