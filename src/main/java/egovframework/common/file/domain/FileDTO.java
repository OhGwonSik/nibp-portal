package egovframework.common.file.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class FileDTO {
    private Long fileOid;
    private String tblNm;
    private Long tblOid;
    private String orgnlFileNm;      // 원본 파일명
    private String strgFileNm;            // 서버 저장 파일명
    private String strgFilePath;            // 파일 저장 경로
    private Long strgFileCpct;              // 파일 크기

    private String strgSmlFileNm;            // 서버 저장 파일명 (작은 버전)
    private String strgSmlFilePath;            // 파일 저장 경로 (작은 버전)
    private Long strgSmlFileCpct;              // 파일 크기 (작은 버전)

    private String strgMdFileNm;            // 서버 저장 파일명 (중간 버전)
    private String strgMdFilePath;            // 파일 저장 경로 (중간 버전)
    private Long strgMdFileCpct;              // 파일 크기 (중간 버전)

    private String fileTypeNm;            // 파일 확장자
    private String fileType;            // 파일 타입 (첨부 파일 or ck에디터)

    private String thmbYn;         // 썸네일 여부
    private String imgSbstTxtCn;             // 이미지 대체 텍스트

    private Integer dwnldCnt;        // 다운로드 수
    private Integer atchFileSeq;        // 첨부파일순서
    private String fileExpln;                 // 비고 (이미지 리사이즈 할 때 오류 발생 시 오류 표시 용)
    private String useYn;               // 사용 여부

    private String regId;           // 동록자 ID
    private LocalDateTime regDt;        // 등록일시
    private String mdfcnId;           // 수정자 Id
    private LocalDateTime mdfcnDt;        // 수정일시

    private String storedStrgFileNm;
    private String originalStrgFileNm;
}
