package egovframework.admin.admin500.domain;

import java.util.List;

import egovframework.common.file.domain.FileDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SurveyOptionDTO {
	private Long srvyOid;
	private Long srvyQitemOid;
	private Long srvyQitemOptOid;
    private Integer srvyQitemOptSeq;
    private String srvyQitemOptTxt;
    private String etcOptYn;
    private Long srvyQitemOptImgFileOid;
    private Integer attachFileIndex;
    private List<FileDTO> optionAttach;
    
    private String regId;
    private String mdfcnId;
}
