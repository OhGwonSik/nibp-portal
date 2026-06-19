package egovframework.admin.admin1000.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : Admin1001DTO.java
 * @Description : 부서 관리 DTO
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1001DTO {
    private Long deptOid;           // 부서 ID
    private String deptNm;       // 부서명
    private Long upDeptOid;         // 상위 부서 ID
    private Integer sortSeq;     // 정렬 순서
    private String useYn;          // 사용 여부
    private String regId;      // 등록자 ID
    private String mdfcnId;      // 수정자 ID
}
