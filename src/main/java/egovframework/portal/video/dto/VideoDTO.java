package egovframework.portal.video.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {
    private Long vdoOid;            // 동영상 ID
    private String vdoTtl;          // 동영상 제목
    private String vdoExpln;      // 동영상 설명 (이미지 alt 속성으로 사용)
    private String vdoUrl;         // 동영상 링크 (YouTube URL)
    private String thmbPath;    // 썸네일 이미지 파일 경로
    private Integer inqCnt;          // 조회수
    private String openYn;           // 공개여부
    private String useYn;            // 사용 여부
    private String regId;        // 등록자ID
    private LocalDateTime regDt;     // 등록일시
    private String mdfcnId;        // 수정자ID
    private LocalDateTime mdfcnDt;     // 수정일시

    private VideoDTO prevVideo;      // 이전글
    private VideoDTO nextVideo;      // 다음글
    private String embedUrl;         // iframe용 embed url
}