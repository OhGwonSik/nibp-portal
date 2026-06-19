package egovframework.common.audit.domain;

import java.time.LocalDateTime;

import egovframework.common.annotation.Encrypted;
import egovframework.common.annotation.Masked;
import egovframework.common.enums.MaskingType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class PersonalInfoProcLog {
    private Long prvcPrcsProcLogOid;
    private LocalDateTime acsDt;
    private Long acsuserOid;
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
    private LocalDateTime regDt;
    private String regId;
}
