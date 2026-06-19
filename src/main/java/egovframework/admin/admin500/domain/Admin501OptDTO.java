package egovframework.admin.admin500.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin501OptDTO {
    private Long srvyQitemOptOid;
    private Long srvyQitemOid;          // old
    private Integer srvyQitemOptSeq;
    private String srvyQitemOptTxt;
    private Long srvyQitemOptImgFileOid;
    private String etcOptYn;
    private String regId;
}