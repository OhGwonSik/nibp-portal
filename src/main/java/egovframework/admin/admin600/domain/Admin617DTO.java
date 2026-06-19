package egovframework.admin.admin600.domain;

import egovframework.common.file.domain.AttachedFileDTO;
import egovframework.common.file.domain.FileDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class Admin617DTO {
    private Long cardNewsOid;             // BIGINT(20) 카드뉴스 ID

    @NotBlank(message = "카드뉴스 제목은 필수 입력값입니다.")
    @Size(min = 1, max = 100, message = "카드뉴스 제목은 1자 이상 100자 이하로 입력해주세요.")
    private String cardNewsNm;           // VARCHAR(100) 카드뉴스 제목

    private String smry;        // text 요약 텍스트

    @NotBlank(message = "썸네일 파일은 필수 입력값입니다.")
    @Size(max = 255, message = "썸네일 파일 경로는 최대 255자까지 입력 가능합니다.")
    private String thmbPath;  // VARCHAR(255) 썸네일 이미지 경로

    private String thumbnailAltText; // 썸네일 대체 텍스트
    private Long thumbnailAttachNo;  // 썸네일 첨부파일 번호 (수정 시 altText 업데이트용)

    private Integer inqCnt;        // INT(11) 조회수

    @NotBlank(message = "공개 여부는 필수 입력값입니다.")
    @Pattern(regexp = "^[YN]$", message = "공개 여부는 Y 또는 N만 허용됩니다.")
    private String openYn;         // CHAR(1) 공개 여부

    @Pattern(regexp = "^[YN]$", message = "사용 여부는 Y 또는 N만 허용됩니다.")
    private String useYn;          // CHAR(1) 사용 여부

    private String regId;      // 등록자 ID
    private LocalDateTime regDt;   // 등록일시
    private String mdfcnId;      // 수정자 ID
    private LocalDateTime mdfcnDt;   // 수정일시

    private List<FileDTO> cardNewsFiles;
    private List<Long> deleteCnAttachNos; // 삭제할 첨부파일 번호 목록 (수정 시 사용)
    private List<AttachedFileDTO> attachedFiles;
    private List<FileAltTextInfo> fileAltTexts; // 파일별 대체 텍스트 정보

    @Getter
    @Setter
    @ToString
    public static class FileAltTextInfo {
        private Long fileOid;        // 기존 파일 번호 (수정 시)
        private Integer atchFileSeq;// 첨부 순서
        private String imgSbstTxtCn;     // 대체 텍스트
        private String fileName;    // 파일명
    }
}
