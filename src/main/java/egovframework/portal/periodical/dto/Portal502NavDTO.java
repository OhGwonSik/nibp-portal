package egovframework.portal.periodical.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : Portal502NavDTO.java
 * @Description : 정기발간자료 이전글/다음글 DTO
 *
 * @author : j.h.kim
 * @since : 2025. 01. 13
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Portal502NavDTO {
    private Long fxtmPblsDataOid;   // 정기발간자료 번호
    private String fxtmPblsDataTtl; // 총권 제목
}
