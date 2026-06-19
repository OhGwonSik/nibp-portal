package egovframework.admin.admin1000.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @ClassName : Admin1002VO.java
 * @Description : 부서 구성원 관리 VO
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 08
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1002VO {
    private Long deptMmbrOid;
    private Long deptOid;
    private String deptNm;
    private String userNm;          // 사용자명
    private String telno;           // 이메일
    private String jbgd;            // 직책
    private Integer indctSeq;       // 표시 순서
    private String chrgJob;         // 담당 업무
    private String regId;
    private LocalDateTime regDt;
    private String mdfcnId;
    private LocalDateTime mdfcnDt;
}
