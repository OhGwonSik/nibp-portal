package egovframework.common.audit.domain;

import java.time.LocalDateTime;

import egovframework.common.annotation.Encrypted;
import egovframework.common.annotation.Masked;
import egovframework.common.enums.DeviceType;
import egovframework.common.enums.MaskingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiAccessLog {
    private Long userAcsLogOid;
    private Long userOid;
    private String userId;
    private String acsType; // API 또는 PAGE
    private String rqtUri;
    private String rqtMthd;
    @Encrypted
    @Masked(type = MaskingType.ALL)
    private String rqtQry;
    private Integer rqtBdySz;
    private Integer sttsCd;
    private Integer rspnsBdySz;
    private Long rspnsTimeMs;
    private String errorMsg;
    @Encrypted
    @Masked(type = MaskingType.ALL)
    private String rfprUrl;
    private String rfprType;
    @Encrypted
    @Masked(type = MaskingType.ALL)
    private String srchKywd;
    @Encrypted
    @Masked(type = MaskingType.IP_ADDRESS)
    private String ipAddr;
    private String userAgt;
    private String brwsrNm;
    private String brwsrVer;
    private String osNm;
    private String osVer;
    private DeviceType dvcType;
    private String ntnCd;
    @Encrypted
    @Masked(type = MaskingType.ALL)
    private String ctyNm;
    private String ssnId;
    private String regId;
    private LocalDateTime regDt;
}