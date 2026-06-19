package egovframework.portal.survey.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SurveyInfoDTO {
	
	private Long courseNo;
	private Long srvyOid;
	private Long userOid;
	private String srvyTtl;
	private String srvyBgngDt;
	private String srvyEndDt;
	private Integer qstCnt;
	private String stts;
	private Integer fileCnt;
}
