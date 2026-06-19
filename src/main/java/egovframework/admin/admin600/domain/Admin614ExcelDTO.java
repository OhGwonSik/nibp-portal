package egovframework.admin.admin600.domain;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin614ExcelDTO {
    private Long popupOid;             // 팝업 ID
    private String popupTtl;           // 팝업 제목
    private String popupType;         // 팝업 유형 (I: Image, H: HTML)
    private LocalDate popupBgngDt;        // 게시 시작 일시
    private LocalDate popupEndDt;          // 게시 종료 일시 (NULL이면 무기한)
    private String openState;         // 공개상태(대기, 게재, 게재 종료)
}