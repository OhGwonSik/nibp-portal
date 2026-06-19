package egovframework.admin.admin600.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class Admin618DeleteDTO {
    private Long popupZoneOid;                 // 팝업존 ID
    private List<Long> popupZoneOids;          // 팝업존 ID 목록

    private String useYn;                     // 사용 여부
    private String mdfcnId;                 // 수정자 ID
    private LocalDateTime mdfcnDt;              // 수정일시
}
