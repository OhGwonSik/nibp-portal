package egovframework.admin.admin1100.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName : Admin1101SectionVO.java
 * @Description : 정기발간자료 섹션 VO (특집논문, 논문, 부록)
 *
 * @author : j.h.kim
 * @since : 2025. 01. 12
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1101SectionVO {
    private Long fxtmPblsSectOid;       // 섹션번호
    private Long fxtmPblsDataOid;       // 정기발간자료 번호
    private String fxtmPblsSectType;    // 섹션타입(SPECIAL:특집논문, PAPER:논문, APPENDIX:부록)
    private String fxtmPblsSectTtl;     // 섹션제목 (특집논문의 경우 주제)
    private Integer sortSeq;            // 정렬순서
    private String regId;           // 등록자ID
    private LocalDateTime regDt;        // 등록일시
    private String mdfcnId;           // 수정자ID
    private LocalDateTime mdfcnDt;        // 수정일시
    
    // 하위 아이템 목록
    private List<Admin1101ItemVO> items;
}
