package egovframework.admin.admin600.domain;

import egovframework.common.annotation.HtmlTextMaxSize;
import egovframework.common.constant.Constants;
import egovframework.common.file.domain.AttachedFileDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class Admin614DTO {
    private Long popupOid;                       // BIGINT(20) 팝업 ID
    @NotBlank(message = "팝업 제목은 필수 입력값입니다.")
    @Size(min = 1, max = 100, message = "팝업 제목은 1자 이상 100자 이하로 입력해주세요.")
    private String popupTtl;                     // VARCHAR(100) 팝업 제목
    @NotBlank(message = "팝업 유형은 필수 입력값입니다.")
    @Pattern(regexp = "^[IH]$", message = "팝업 유형은 I 또는 H만 허용됩니다.")
    private String popupType;                   // CHAR(1) 팝업 유형 (I: Image, H: HTML)
    @HtmlTextMaxSize(max = Constants.MAX_TEXT_LENGTH, maxHtml = Constants.MAX_HTML_LENGTH, message = "HTML 내용은 최대 " + Constants.MAX_TEXT_LENGTH + "자까지 입력 가능합니다.")
    private String popupCn;                 // TEXT HTML 타입일 때, CKEditor 내용
    private String htmlContentText;             // TEXT HTML 내용의 텍스트 버전
    @Size(max = 255, message = "이미지 파일 경로는 최대 255자까지 입력 가능합니다.")
    private String popupImgFilePath;               // VARCHAR(255) Image 타입일 때 이미지 경로
    @Size(max = 255, message = "링크 URL은 최대 255자까지 입력 가능합니다.")
    private String popupUrlAddr;                     // VARCHAR(255) 클릭 시 이동 URL

    @NotNull(message = "게시 시작 일시는 필수 입력값입니다.")
    private LocalDate popupBgngDt;                  // TIMESTAMP 게시 시작 일시
    private LocalDate popupEndDt;                    // TIMESTAMP 게시 종료 일시 (NULL이면 무기한)

    private Integer pstnX;                  // INT(11) 팝업 X좌표 (Left)
    private Integer pstnY;                  // INT(11) 팝업 Y좌표 (Top)
    private Integer sizeW;                      // INT(11) 팝업 너비
    private Integer sizeH;                     // INT(11) 팝업 높이

    @NotBlank(message = "프론트 노출 여부는 필수 입력값입니다.")
    @Pattern(regexp = "^[YN]$", message = "프론트 노출 여부는 Y 또는 N 값만 허용됩니다.")
    private String openYn;                      // CHAR(1) 프론트 노출 여부 (Y: 공개, N: 비공개)
    private String useYn;                       // CHAR(1) 사용 여부 (Y: 사용, N: 미사용)

    private String regId;                   // VARCHAR(10) 등록자 ID
    private LocalDateTime regDt;                // TIMESTAMP 등록일시
    private String mdfcnId;                   // VARCHAR(10) 수정자 ID
    private LocalDateTime mdfcnDt;                // TIMESTAMP 수정일시

    private List<Long> deletedFiles;            // 삭제할 첨부파일 번호 목록
    private String tempUploadKey;               // 임시 업로드 키
    private String rfrncSeCd;                   // 참조 구분 코드
    private List<Long> deleteAttachNos;         // 삭제할 첨부파일 번호 목록
    private List<AttachedFileDTO> attachedFiles;// 첨부 파일 목록
    private List<AttachedFileDTO> editorFiles;  // CKEditor 이미지 파일 목록
    private List<Long> deleteEditorAttachNos;   // 삭제할 CKEditor 이미지 번호 목록
}