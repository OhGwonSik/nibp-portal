package egovframework.portal.user.domain;

import egovframework.common.annotation.Encrypted;
import egovframework.common.annotation.Masked;
import egovframework.common.enums.MaskingType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class User {

    private Long userOid;

    private String userNmKorn;

    private String userNmEng;

    private String brdt;

    private String userId;

    private String pswd;
    private String userAuthrt;
    private String userType;
    private String userStts;

    @Encrypted
    @Masked(type = MaskingType.EMAIL_LOCAL_ONLY)
    private String emlLcal;

    private String emlDmn;
    private String emlInpTyp;

    private String mpnoPfx;

    @Encrypted
    @Masked(type = MaskingType.PHONE_MIDDLE_ONLY)
    private String mpnoMid;

    private String mpnoSfx;

    private String instOid;

    private String instNm;

    private String deptCd;

    private String deptNm;

    private String jbpsCd;

    private String jbpsNm;

    private Integer lgnFailCnt;
    private String prvcUseYn;
    private String prvcPvsnAgreYn;
    private String trmsAgreYn;
    private String tdptyPvsnAgreYn;
    private String cnsgnPvsnAgreYn;
    private String emlRcptnAgreYn;
    private String useYn;
    private String regId;
    private LocalDateTime regDt;
    private String mdfcnId;
    private LocalDateTime mdfcnDt;
    private LocalDateTime lastLoginDt;
}
