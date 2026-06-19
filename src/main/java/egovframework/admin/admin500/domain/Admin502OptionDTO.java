package egovframework.admin.admin500.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin502OptionDTO {

	private Long srvyQitemOptOid;         // 객관식 보기 번호
    private String srvyQitemOptTxt;      // 보기 텍스트
    private int srvyQitemOptSeq;         // 보기 순서
    
    private Integer scaleValue; // [LIKERT] 점수 (1, 2, 3...)
    private Double averageValue;// [RATIO] 평균 비율 값
    
    private long respCount;     // 응답 수 (공통)
}
