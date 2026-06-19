package egovframework.admin.admin500.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin501QstDTO {
    private Long srvyQitemOid;          // old or new
    private Long upSrvyQitemOid;    // 부모 번호
}