package egovframework.admin.admin500.domain;

import egovframework.common.annotation.Encrypted;
import egovframework.common.annotation.Masked;
import egovframework.common.enums.MaskingType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin502ResponseDTO {

    private String srvyQitemOptTxt;      // [RANK] 항목명
    private String srvyRspnsTxt;     // [SHORT/LONG] 주관식 답변
    private Integer srvyRspnsRank;   // [RANK] 순위

    private Long srvyRspnsFileOid;    // [IMG_RESP] 업로드 파일 번호 안쓰는 지 체크
    private String respStrgFilePath;
    private String respStrgFileNm;
    private String respOrgnlFileNm;

    private String regDt;       // 등록일
    @Encrypted
    @Masked(type = MaskingType.NAME_KR)
    private String srvyRspdntNm;      // 응답자명
    private long respCount;     // [RANK] 득표수
}
