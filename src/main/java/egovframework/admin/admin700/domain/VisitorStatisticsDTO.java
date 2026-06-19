package egovframework.admin.admin700.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VisitorStatisticsDTO {
    private String statYear; // 통계년
    private int prevAcc; // 동년 이전달 누적
    private int targetVal; // 이번달 값
}
