package egovframework.admin.admin600.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class Admin618ExcelDTO {
    private Long popupZoneOid;                 // 팝업존 ID
    private String popupZoneNm;               // 제목
    private LocalDate regDt;           // 등록일시
    private String openYn;             // 공개 여부
    private String openState;          // 공개 여부 텍스트
    private Integer inqCnt;            // 조회수
}
