package egovframework.admin.admin600.domain;

import egovframework.common.file.domain.FileDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class Admin617VO {
    private Long cardNewsOid;                 // 카드뉴스 ID
    private String cardNewsNm;               // 제목
    private String smry;            // 요약
    private String thmbPath;      // 썸네일 경로
    private Integer inqCnt;            // 조회수
    private String openYn;             // 공개 여부
    private String useYn;              // 사용 여부

    private String regId;          // 등록자 ID
    private LocalDateTime regDt;       // 등록일시
    private String mdfcnId;          // 수정자 ID
    private LocalDateTime mdfcnDt;       // 수정일시

    private String openState;          // 공개 상태

    // 첨부파일 목록
    private List<FileDTO> files;
}
