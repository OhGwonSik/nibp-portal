package egovframework.common.organization.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName : OrganizationDTO.java
 * @Description : 조직도 부서 DTO
 *
 * @author : j.h.kim
 * @since  : 2025. 12. 30
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class OrganizationDTO {
    private Long deptOid;           // 부서 고유번호
    private String deptNm;       // 부서명
    private Long upDeptOid;         // 상위 부서 ID
    private Integer sortSeq;     // 정렬 순서
    private String useYn;          // 사용 여부
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    
    // 조직도 렌더링용
    private List<OrganizationDTO> children = new ArrayList<>();  // 하위 부서
    private List<DeptMemberDTO> members = new ArrayList<>();     // 소속 멤버
}
