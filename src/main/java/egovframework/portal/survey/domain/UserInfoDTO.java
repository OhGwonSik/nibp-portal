package egovframework.portal.survey.domain;

import egovframework.common.annotation.Encrypted;
import egovframework.common.annotation.Masked;
import egovframework.common.enums.MaskingType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserInfoDTO {
    private String srvyRspdntNm; 
    
    @Encrypted
    @Masked(type = MaskingType.ORGANIZATION)
    private String srvyRspdntInst; 
    
    private String srvyRspdntGndr; 
    
    @Encrypted
    @Masked(type = MaskingType.PHONE_MIDDLE)
    private String srvyRspdntMpno; 
    @Encrypted
    @Masked(type = MaskingType.EMAIL_LOCAL)
    private String srvyRspdntEml; 

    private String srvyRspdntAddr;
}
