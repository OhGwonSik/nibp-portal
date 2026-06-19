package egovframework.admin.admin1000.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @ClassName : Admin1001VO.java
 * @Description : 부서 관리 VO
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1001VO {
    private Long deptOid;           // 부서 ID
    private String deptNm;       // 부서명
    private Long upDeptOid;         // 상위 부서 ID
    private String parentName;     // 상위 부서명
    private Integer sortSeq;     // 정렬 순서
    private String useYn;          // 사용 여부
    private Long memberCount;      // 소속 구성원 수
    private Long childCount;       // 하위 부서 수
    private String regId;      // 등록자 ID
    private LocalDateTime regDt;   // 등록일시
    private String mdfcnId;      // 수정자 ID
    private LocalDateTime mdfcnDt;   // 수정일시
}
