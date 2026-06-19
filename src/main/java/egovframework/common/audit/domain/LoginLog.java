package egovframework.common.audit.domain;

import java.time.LocalDateTime;

import egovframework.common.annotation.Encrypted;
import egovframework.common.annotation.Masked;
import egovframework.common.audit.enums.LoginResult;
import egovframework.common.enums.DeviceType;
import egovframework.common.enums.MaskingType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class LoginLog {
    private Long lgnLogOid;
    private Long userOid;
    private String userId;
    private LocalDateTime lgnDt;
    private LoginResult lgnRslt;
    private String lgnFailRsn;
    @Encrypted
    @Masked(type = MaskingType.IP_ADDRESS)
    private String ipAddr;
    private String userAgt;
    private String brwsrNm;
    private String brwsrVer;
    private String osNm;
    private String osVer;
    private DeviceType dvcType;
    private String ssnId;
    private LocalDateTime lgtDt;
    private Integer ssnContnHr;
    private String ntnCd;
    @Encrypted
    @Masked(type = MaskingType.ALL)
    private String ctyNm;
    private String regId;
    private LocalDateTime regDt;
    private String mdfcnId;
    private LocalDateTime mdfcnDt;
}
