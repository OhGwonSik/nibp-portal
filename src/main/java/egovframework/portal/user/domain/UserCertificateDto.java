package egovframework.portal.user.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class UserCertificateDto {

    private Long userCertificateNo;

    private Long userOid;          // user_oid
    private String orgnlFileNm; // origin_file_name (원본 파일명)
    private String strgFileNm;       // file_name (서버 저장 파일명)
    private String strgFilePath;       // file_path
    private Long strgFileCpct;         // file_size
    private String fileTypeNm;        // file_ext
    private String regId;      // reg_id

    private String mdfcnId;      // upd_user_id
}
