package egovframework.common.board.dto;

import egovframework.common.board.domain.BaseAttachDTO;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BoardAttachDTO extends BaseAttachDTO {

    private Long attachNo;         // 첨부파일번호
    private Long bbsPstOid;           // 게시글번호
    private String strgFileNm;       // 서버 저장 파일명
    private String orgnlFileNm; // 원본 파일명
    private String strgFilePath;       // 파일경로
    private Long strgFileCpct;         // 파일크기
    private String fileTypeNm;        // 파일확장자
    private String fileType;       // 파일 타입(첨부파일 or ckeditor)
    private String thmbPath;  // 썸네일경로
    private String thmbYn;    // 썸네일여부
    private Integer imageWidth;    // 이미지너비
    private Integer imageHeight;   // 이미지높이
    private Integer dwnldCnt;   // 다운로드수
    private Integer atchFileSeq;   // 첨부파일순서
    private String useYn;          // 사용여부
    private String regId;      // 등록자ID
    private LocalDateTime regDt;   // 등록일시
}
