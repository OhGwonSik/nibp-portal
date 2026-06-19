package egovframework.common.file.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileVO {
    // t_wpo_file_info
    private String fileId;
    private String fileNm;
    private String rgtrId;
    private String fileExtn;


    // t_wpo_file_ref
    private String rfrncKeyId;
    private String rfrncSeCd;

    // View를 위한 포맷팅된 데이터
    private String regDtFormatted;
    private String fileSizeFormatted;

    private Long fileOid;
    private String tblNm;
    private Long tblOid;
    private String orgnlFileNm;
    private String strgFileNm;
    private String strgFilePath;
    private Long strgFileCpct;
    private String strgSmlFileNm;
    private String strgSmlFilePath;
    private Long strgSmlFileCpct;
    private String strgMdFileNm;
    private String strgMdFilePath;
    private Long strgMdFileCpct;
    private String fileTypeNm;
    private String fileType;
    private Integer dwnldCnt;
    private Integer atchFileSeq;
    private String useYn;
    private String regId;
    private LocalDateTime regDt;
    private String mdfcnId;
    private LocalDateTime mdfcnDt;
}