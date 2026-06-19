package egovframework.common.board.dto;

import egovframework.common.annotation.HtmlTextMaxSize;
import egovframework.common.constant.Constants;
import egovframework.common.file.domain.FileDTO;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BoardPostInsertDTO {
    private Long bbsPstOid;                         // BIGINT(20) 게시글 ID
    private Long bbsOid;                        // BIGINT(20) 게시판 번호
    private Long upBbsPstOid;                   // BIGINT(20) 부모 게시글 ID
    private Long userOid;                         // BIGINT(20) 회원 번호 (비회원 시 NULL)
    private String menuCd;

    private String wrtrNm;                     // VARCHAR(50) 작성자명
    @Pattern(
            regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^*+=-])[A-Za-z\\d!@#$%^*+=-]{9,14}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    private String wrtrPswd;                     // VARCHAR(255) 비회원 비밀번호
    @NotBlank(message = "제목은 필수 입력값입니다.")
    @Size(max = 255, message = "제목은 최대 255자까지 입력 가능합니다.")
    private String bbsPstTtl;                    // VARCHAR(255) 제목
    @NotBlank(message = "내용은 필수 입력값입니다.")
    @HtmlTextMaxSize(max = Constants.MAX_TEXT_LENGTH, maxHtml = Constants.MAX_HTML_LENGTH, message = "내용은 최대 " + Constants.MAX_TEXT_LENGTH + "자까지 입력 가능합니다.")
    private String bbsPstCn;                  // TEXT 내용 (CKEditor 등 HTML 저장)
    private String bbsPstCnTxt;                  // TEXT 내용 (태그 제거)
    private String ctgry;                     // VARCHAR(50) 분류
    private String prvtPstYn;                     // CHAR(1) 비밀글 여부
    private String ntcYn;                     // CHAR(1) 공지 여부
    private String upendFixYn;                     // CHAR(1) 상단 고정 여부
    private LocalDate bgngDt;                   // 시작일
    private LocalDate endDt;                     // 종료일
    private String stts;                       // VARCHAR(20) 상태
    private Integer inqCnt;                     // INT(11) 조회수
    private String delYn;                        // CHAR(1) 삭제 여부
    private String openYn;                       // 공개여부
    private String rcrtType;                  // 모집유형
    private LocalDate regDt;                     // TIMESTAMP 등록일시
    private LocalDate mdfcnDt;                     // TIMESTAMP 수정일시
    private String regId;                    // VARCHAR(50) 등록자 ID
    private String mdfcnId;                    // VARCHAR(50) 수정자 ID
    private String kwrd;						//VARCHAR(255) 키워드
    private String isAdmin;
    
    private String tempSaveYn;                   // 초안저장여부
    
    private List<Long> deleteAttachNos;          // 첨부파일 삭제 리스트
    private List<Long> deleteEditorAttachNos;
    private List<FileDTO> attachedFiles;
    private List<FileDTO> editorFiles;
    private String firstFileAltText;             // 첫 번째 파일의 대체 텍스트
    
    // qna 만족도 평가
    private String tblNm; // 대상 테이블명
    private Long tblOid; // 대상 테이블 PK
    // private String regId; // 등록자(답글자) ID
}