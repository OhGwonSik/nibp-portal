package egovframework.admin.admin600.domain;

import egovframework.common.annotation.HtmlTextMaxSize;
import egovframework.common.constant.Constants;
import egovframework.common.file.domain.AttachedFileDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
public class Admin610DTO {
    private Long qnaOid;                          // BIGINT(20) QnA ID
    private Long upQnaOid;                       // BIGINT(20) 부모 QnA ID

    private String userId;                       // VARCHAR(50) 회원 ID (비회원 시 NULL)

    @Size(max = 50, message = "작성자명은 최대 50자까지 입력 가능합니다.")
    private String wrtNm;                     // VARCHAR(50) 작성자명

    @Size(max = 255, message = "비밀번호는 최대 255자까지 입력 가능합니다.")
    private String pswd;                          // VARCHAR(255) 비회원 비밀번호

    @Size(max = 255, message = "제목은 최대 255자까지 입력 가능합니다.")
    private String qnaTtl;                        // VARCHAR(255) 제목

    @NotBlank(message = "내용은 필수 입력값입니다.")
    @HtmlTextMaxSize(max = Constants.MAX_TEXT_LENGTH, maxHtml = Constants.MAX_HTML_LENGTH, message = "내용은 최대 " + Constants.MAX_TEXT_LENGTH + "자까지 입력 가능합니다.")
    private String qnaCn;                      // TEXT 내용 (HTML 저장)

    @Size(max = 100, message = "분류는 최대 100자까지 입력 가능합니다.")
    private String ctgry;                        // VARCHAR(100) 분류

    @Pattern(regexp = "^[YN]$", message = "답변 여부는 Y 또는 N만 허용됩니다.")
    private String ansYn;                     // CHAR(1) 답변 완료 여부

    @Pattern(regexp = "^[YN]$", message = "비공개글 여부는 Y 또는 N만 허용됩니다.")
    private String prvtPstYn;                     // CHAR(1) 비공개글 여부

    @Pattern(regexp = "^[YN]$", message = "사용 여부는 Y 또는 N만 허용됩니다.")
    private String useYn;                        // CHAR(1) 사용 여부

    private Integer inqCnt;                     // INT(11) 조회수

    private LocalDate regDt;                     // TIMESTAMP 등록일시
    private LocalDate mdfcnDt;                     // TIMESTAMP 수정일시

    private List<AttachedFileDTO> editorFiles;   // ckeditor 파일
    private List<Long> deletedFiles;             // 삭제할 첨부파일 번호 목록

    // qna 만족도 평가
    private String tblNm; // 대상 테이블명
    private Long tblOid; // 대상 테이블 PK
    private String regId; // 등록자(답글자) ID

    public static void populateDtoFields(EgovMap target, Admin610DTO admin610DTO) {
        target.put("qnaOid", admin610DTO.getQnaOid());
        target.put("upQnaOid", admin610DTO.getUpQnaOid());
        target.put("userId", admin610DTO.getUserId());
        target.put("wrtNm", admin610DTO.getWrtNm());
        target.put("pswd", admin610DTO.getPswd());
        target.put("qnaTtl", admin610DTO.getQnaTtl());
        target.put("ctgry", admin610DTO.getCtgry());
        target.put("qnaCn", admin610DTO.getQnaCn());
        target.put("ansYn", admin610DTO.getAnsYn());
        target.put("prvtPstYn", admin610DTO.getPrvtPstYn());
        target.put("useYn", admin610DTO.getUseYn());
        target.put("inqCnt", admin610DTO.getInqCnt());
        target.put("regDt", admin610DTO.getRegDt());
        target.put("mdfcnDt", admin610DTO.getMdfcnDt());
    }
}
