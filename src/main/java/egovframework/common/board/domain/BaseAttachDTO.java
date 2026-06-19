package egovframework.common.board.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BaseAttachDTO {
	// 파일 공통
    private String useYn;
    private String strgFilePath;
    private String strgFileNm;
    private String orgnlFileNm;
}
