package egovframework.admin.admin700.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QnaSatisfactionStatistcsDTO {
    private String monthGrp;
    private Long publicPost;
    private Long privatePost;
    private Long satisfied;
    private Long unsatisfied;
    private Long noResponse;
    private Long total;
    private Double publicPostRate;
    private Double privatePostRate;
}
