package egovframework.common.file.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AttachedFileDTO {
    // temp에 임시 업로드 할 때 응답으로 받은 것 저장하는 용도
    private String originalStrgFileNm;
    private String storedStrgFileNm;
    private String strgFilePath;
    private Long strgFileCpct;
    private String fileTypeNm;
    private String fileType;
}
