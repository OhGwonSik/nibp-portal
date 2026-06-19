package egovframework.portal.survey.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SurveySubmitReqDTO {
	// 설문 번호
    private Long srvyOid;
    private Long courseNo;
    
    private Long srvyRspdntOid;
    private Long userOid;
    
    private String ipAddr;

    // 응답자 기본 정보
    private UserInfoDTO userInfo;

    // 답변 목록
    private List<AnswerDTO> answerList;
    
    private String completionYn;
    private String completionStatus;
    
    private String regId;
    private String mdfcnId;
}
