package egovframework.admin.admin1100.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @ClassName : Admin1101DeleteDTO.java
 * @Description : 정기발간자료 삭제 DTO
 *
 * @author : j.h.kim
 * @since : 2025. 01. 12
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1101DeleteDTO {
    @NotEmpty(message = "삭제할 항목을 선택해주세요.")
    private List<Long> fxtmPblsDataOids; // 삭제할 정기발간자료 번호 목록
}
