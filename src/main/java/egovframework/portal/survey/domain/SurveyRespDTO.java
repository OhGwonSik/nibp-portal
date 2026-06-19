package egovframework.portal.survey.domain;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SurveyRespDTO {
	private Long srvyRspnsOid;        // 응답번호 (PK)
    private Long srvyRspdntOid;        // 응답자번호 (FK)
    private Long srvyQitemOid;         // 문항번호 (FK)
    private Long srvyQitemOptOid;         // 선택지번호 (FK, Nullable)
    
    private String srvyRspnsTxt;     // 응답텍스트 (주관식/기타의견)
    private BigDecimal srvyRspnsNo; // 응답숫자 (점수/비율) - decimal 대응
    private Integer srvyRspnsRank;   // 응답순위 (순위형)
    private Long srvyRspnsFileNo;    // 응답파일번호 (이미지)
    
    private String regId;   // 등록자ID
    private String mdfcnId;   // 수정자ID
}
