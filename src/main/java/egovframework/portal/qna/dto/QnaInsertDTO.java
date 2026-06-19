package egovframework.portal.qna.dto;

import egovframework.common.annotation.HtmlTextMaxSize;
import egovframework.common.constant.Constants;
import egovframework.common.file.domain.AttachedFileDTO;
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
public class QnaInsertDTO {
    private Long qnaOid;                          // BIGINT(20) QnA ID
    private Long upQnaOid;                       // BIGINT(20) 부모 QnA ID
    private String userId;                       // VARCHAR(50) 회원 ID (비회원 시 NULL)
    @NotBlank(message = "작성자명은 필수 입력값입니다.")
    private String wrtrNm;                     // VARCHAR(50) 작성자명
    @NotBlank(message = "카테고리는 필수 입력값입니다.")
    private String ctgry;                     // VARCHAR(100) 카테고리
    @Pattern(
    	    regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^*+=-])[A-Za-z\\d!@#$%^*+=-]{9,14}$",
    	    message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
    	)
    private String pswd;                          // VARCHAR(255) 비회원 비밀번호
    @NotBlank(message = "제목은 필수 입력값입니다.")
    @Size(max = 255, message = "제목은 최대 255자까지 입력 가능합니다.")
    private String qnaTtl;                        // VARCHAR(255) 제목
    @NotBlank(message = "내용은 필수 입력값입니다.")
    @HtmlTextMaxSize(max = Constants.MAX_TEXT_LENGTH, maxHtml = Constants.MAX_HTML_LENGTH, message = "내용은 최대 " + Constants.MAX_TEXT_LENGTH + "자까지 입력 가능합니다.")
    private String ntcCn;                      // TEXT 내용 (CKEditor 등 HTML 저장)
    private String ntcCnTxt;                      // TEXT 내용 태그제거
    private String ansYn;                     // CHAR(1) 답변 완료 여부
    @NotBlank(message = "비밀글 여부는 필수 입력값입니다.")
    private String prvtPstYn;                     // CHAR(1) 비밀글 여부
    private String useYn;                        // CHAR(1) 사용 여부
    private Integer inqCnt;                     // INT(11) 조회수
    private LocalDate regDt;                     // TIMESTAMP 등록일시
    private LocalDate mdfcnDt;                     // TIMESTAMP 수정일시
    private String mdfcnId;

    private List<AttachedFileDTO> editorFiles;
    
    private List<Long> deleteAttachNos;

    private List<Long> deleteEditorAttachNos;
}