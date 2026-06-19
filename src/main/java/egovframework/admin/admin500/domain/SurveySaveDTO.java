package egovframework.admin.admin500.domain;

import java.util.List;

import egovframework.common.file.domain.AttachedFileDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SurveySaveDTO {
    private Long srvyOid;
    private String srvyTtl;
    private String srvyBgngDt;
    private String srvyEndDt;
    private String srvyTrgt;
    private String srvyCn;
    private String srvyType;

    private String nmClctYn;
    private String gndrClctYn;
    private String instClctYn;
    private String mpnoClctYn;
    private String emlClctYn;
    private String addrClctYn;

    private String srvyStts;

    private List<SurveyQuestionDTO> qstList;
    private List<Long> deletedQstList;
    private List<Long> deletedOptList;
    // 문항 첨부 삭제 대상(file_oid 리스트). 프론트에서 X를 눌러 삭제한 기존 첨부의 file_no가 담긴다.
    private List<Long> deletedQstAttachList;
    
    private String regId;
    private String mdfcnId;

    // ckeditor 이미지 파일
    private List<AttachedFileDTO> editorFiles;
}
