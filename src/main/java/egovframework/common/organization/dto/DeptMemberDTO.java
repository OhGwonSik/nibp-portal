package egovframework.common.organization.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : DeptMemberDTO.java
 * @Description : 조직도 구성원 DTO
 *
 * @author : j.h.kim
 * @since  : 2025. 12. 30
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class DeptMemberDTO {
    private Long deptMmbrOid;
    private Long deptOid;
    private String userNm;          // 사용자명
    private String telno;           // 전화번호
    private String jbgd;            // 직급/직책
    private Integer indctSeq;       // 표시 순서
    private String chrgJob;         // 담당 업무
}
