package egovframework.admin.admin600.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class Admin606DeleteDTO {
    private Long faqDtlOid;            // FAQ ID
    @NotNull(message = "삭제할 FAQ가 존재하지 않습니다.")
    private List<Long> faqDtlOids;     // FAQ ID 목록
    private String mdfcnId;      // 수정자 ID
    private String useYn;          // 사용 여부
    private LocalDateTime mdfcnDt;   // 수정일시
}