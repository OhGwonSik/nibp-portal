package egovframework.portal.popup.dto;

import egovframework.common.file.domain.FileDTO;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PopupDTO {
    private Long popupOid;                   // 팝업 ID
    private String popupTtl;                 // 팝업 제목
    private String popupType;               // 팝업 유형 (I: Image, H: HTML)
    private String popupCn;             // HTML 타입일 때 내용
    private String popupImgFilePath;           // Image 타입일 때 이미지 경로
    private String popupUrlAddr;                 // 클릭 시 이동 URL
    private LocalDate popupBgngDt;              // 게시 시작 일시
    private LocalDate popupEndDt;                // 게시 종료 일시 (NULL이면 무기한)
    private Integer pstnX;              // 팝업 X좌표 (Left)
    private Integer pstnY;              // 팝업 Y좌표 (Top)
    private Integer sizeW;                  // 팝업 너비
    private Integer sizeH;                 // 팝업 높이
    private String openYn;                  // 프론트 노출 여부 (Y: 공개, N: 비공개)
    private String useYn;                   // 사용 여부 (Y: 사용, N: 미사용)
    private String regId;               // 등록자 ID
    private LocalDateTime regDt;            // 등록일시
    private String mdfcnId;               // 수정자 ID
    private LocalDateTime mdfcnDt;            // 수정일시
    private String openState;               // 공개상태(대기, 게재, 게재 종료)
    private List<FileDTO> attachments;      // 첨부파일 정보
}