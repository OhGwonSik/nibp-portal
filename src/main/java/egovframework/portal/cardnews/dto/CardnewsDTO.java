package egovframework.portal.cardnews.dto;

import egovframework.common.file.domain.FileDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CardnewsDTO {
    private Long cardNewsOid;                 // 카드뉴스 ID
    private String cardNewsNm;               // 제목
    private String smry;            // 요약
    private String cnLink;             // 링크 URL
    private String thmbPath;      // 썸네일 경로
    private String thumbnailAltText;   // 썸네일 대체 텍스트
    private Integer inqCnt;            // 조회수
    private String openYn;             // 공개 여부
    private String useYn;              // 사용 여부
    private String regId;          // 등록자 ID
    private LocalDateTime regDt;       // 등록일시
    private String mdfcnId;          // 수정자 ID
    private LocalDateTime mdfcnDt;       // 수정일시

    private CardnewsDTO prevCardnews;  // 이전글
    private CardnewsDTO nextCardnews;  // 다음글

    List<FileDTO> attachments;
}