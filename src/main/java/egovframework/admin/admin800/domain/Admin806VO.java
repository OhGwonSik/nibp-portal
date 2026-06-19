package egovframework.admin.admin800.domain;

import egovframework.common.annotation.Encrypted;
import egovframework.common.annotation.Masked;
import egovframework.common.enums.MaskingType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin806VO {
    private Long prvcPrcsProcLogOid;
    private String acsDt;
    private Long acsUserNo;
    @Encrypted
    @Masked(type = MaskingType.USER_ID)
    private String acsId;
    @Encrypted
    @Masked(type = MaskingType.IP_ADDRESS)
    private String acsIp;
    private String menuNm;
    private String flfmtTaskDtl;
    @Encrypted
    @Masked(type = MaskingType.ALL)
    private String infoPrcsSubj;
    private String rsn;
    private String regDt;
    @Encrypted
    @Masked(type = MaskingType.USER_ID)
    private String regId;
}
