package egovframework.admin.admin1000.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName : Admin1001DeleteDTO.java
 * @Description : 부서 삭제 DTO
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1001DeleteDTO {
    private List<Long> deptOids;
    private String mdfcnId;
}
