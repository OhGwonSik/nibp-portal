package egovframework.admin.admin500.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin501SurveyCopyDTO {
    private Long oldSrvyOid;     // 원본 설문 번호
    private Long newSrvyOid;     // 복사된 설문 번호 (INSERT 후 생성)
    private String newSrvyTtl;
    private String srvyBgngDt;
    private String srvyEndDt;
    private String userId;
}