package egovframework.portal.periodical.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName : Portal502SectionDTO.java
 * @Description : 정기발간자료 섹션 DTO (특집논문, 논문, 부록)
 *
 * @author : j.h.kim
 * @since : 2025. 01. 13
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Portal502SectionDTO {
    private Long fxtmPblsSectOid;       // 섹션번호
    private String fxtmPblsSectType;    // 섹션타입(SPECIAL:특집논문, PAPER:논문, APPENDIX:부록)
    private String fxtmPblsSectTtl;     // 섹션제목 (특집논문의 경우 주제)
    
    // 하위 아이템 목록
    private List<Portal502ItemDTO> items;
}
