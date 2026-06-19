package egovframework.admin.usermenuauth.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
public class UserMenuAuthDto {
    private Integer userMenuAuthrtOid;
    private Integer userOid;
    private Integer menuOid;
    private String inqAuthrtYn;
    private String wrtAuthrtYn;
    private String delAuthrtYn;
    private String excelAuthrtYn;
    private String otptAuthrtYn;
    private LocalDateTime authBgngDt;
    private LocalDateTime authEndDt;
    private String useYn;
    private String regId;
    private LocalDateTime regDt;
    private String mdfcnId;
    private LocalDateTime mdfcnDt;
}
