package egovframework.portal.popupzone.dto;

import egovframework.common.file.domain.FileDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PopupZoneDTO {
    private Long popupZoneOid;               // 팝업존 ID
    private String popupZoneNm;             // 제목
    private String smry;                 // 요약
    private String popupZoneLink;           // 링크 URL
    private String thmbPath;           // 썸네일 경로
    private String thumbnailAltText;        // 썸네일 대체 텍스트
    private Integer inqCnt;                 // 조회수
    private String openYn;                  // 공개 여부
    private String useYn;                   // 사용 여부
    private String regId;               // 등록자 ID
    private LocalDateTime regDt;            // 등록일시
    private String mdfcnId;               // 수정자 ID
    private LocalDateTime mdfcnDt;            // 수정일시

    private PopupZoneDTO prevPopupZone;     // 이전글
    private PopupZoneDTO nextPopupZone;     // 다음글

    private List<FileDTO> attachments;
}