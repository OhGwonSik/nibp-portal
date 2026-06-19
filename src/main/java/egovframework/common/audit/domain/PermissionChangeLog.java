package egovframework.common.audit.domain;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class PermissionChangeLog {
    private Long prmsnChgLogOid;
    private String chnrgUserId;
    private String trgtUserId;
    private Long userMenuAuthrtOid;
    private Long menuOid;
    private String prmsnType;
    private String oldVl;
    private String newVl;
    private String chgType; // ADD, MODIFY, REMOVE
    private LocalDateTime chgDt;
    private String rsn;
    private LocalDateTime regDt;
    private String regId;
}
