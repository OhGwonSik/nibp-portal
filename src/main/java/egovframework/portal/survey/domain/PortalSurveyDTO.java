package egovframework.portal.survey.domain;

import java.util.List;

import egovframework.common.file.domain.FileDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PortalSurveyDTO {
    private Long srvyOid;
    private String srvyTtl;
    private String srvyCn;
    private String srvyType;

    private String srvyBgngDt;
    private String srvyEndDt;

    private String srvyTrgt;
    private String nmClctYn;
    private String gndrClctYn;
    private String instClctYn;
    private String mpnoClctYn;
    private String emlClctYn;
    private String addrClctYn;

    private String atchFileUseYn;
    private String srvyStts;

    private String regId;
    private String mdfcnId;

    private List<SurveyQuestionDTO> qstList;
    private List<Long> deletedQstList;
    private List<Long> deletedOptList;
}
