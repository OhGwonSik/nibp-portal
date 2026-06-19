package egovframework.admin.admin1000.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : Admin1002DTO.java
 * @Description : 부서 구성원 관리 DTO
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 08
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1002DTO {
    private Long deptMmbrOid;
    private Long deptOid;
    private String userNm;          // 사용자명
    private String telno;           // 전화번호
    private String jbgd;            // 직책
    private Integer indctSeq;       // 표시 순서
    private String chrgJob;         // 담당 업무
    private String regId;
    private String mdfcnId;
}
