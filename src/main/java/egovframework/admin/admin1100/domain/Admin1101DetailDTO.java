package egovframework.admin.admin1100.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName : Admin1101DetailDTO.java
 * @Description : 정기발간자료 상세 정보 DTO (메인 + 섹션 + 아이템)
 *
 * @author : j.h.kim
 * @since : 2025. 01. 12
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1101DetailDTO {
    private Admin1101VO periodical;                     // 메인 정보
    private List<Admin1101SectionVO> sections;          // 섹션 목록 (특집논문, 논문, 부록)
}
