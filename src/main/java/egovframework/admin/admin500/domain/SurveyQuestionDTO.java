package egovframework.admin.admin500.domain;

import egovframework.common.file.domain.FileDTO;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SurveyQuestionDTO {
	private Long srvyOid;
    private String displayQNo;        // CH1, 1, 1-1
    private String parentDisplayQNo;  // null, CH1, 0, 1
    private Integer level;

    private Long srvyQitemOid;   // UPDATE 모드일 때 있음
    private Long upSrvyQitemOid;
    private Integer srvyQitemSeq;
    private String srvyQitemTtl;
    private String srvyQitemType;

    private String esntlYn;
    private String plrlChcYn;
    private String lmtYn;
    private String etcAddYn;

    private Integer likertMin;
    private Integer likertMax;
    private String likertMinLbl;
    private String likertMaxLbl;

    private Integer srvyQitemLmt;

    private List<SurveyOptionDTO> optList;
    private Integer attachFileIndex; // 프런트 serialize 단계에서 기록한 첨부파일 배열 인덱스

    private String regId;
    private String mdfcnId;

    private List<FileDTO> surveyQstAttach;
}
