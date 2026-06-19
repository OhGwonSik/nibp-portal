package egovframework.admin.admin600.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class Admin614DeleteDTO {
    private Long popupOid;             // 팝업 ID
    private List<String> popupOids;    // 팝업 ID 목록
    private String useYn;             // 사용 여부 (Y: 사용, N: 미사용)
    private String mdfcnId;         // 수정자 ID
    private LocalDateTime mdfcnDt;      // 수정일시
}