package egovframework.admin.admin600.domain;

import egovframework.common.file.domain.FileDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
public class Admin606DetailDTO {
    private Long faqDtlOid;                             // FAQ ID

    @NotNull(message = "카테고리 ID는 필수 입력값입니다.")
    private Long faqCtgryOid;                            // 카테고리 ID (FK)

    @NotBlank(message = "질문 내용은 필수 입력값입니다.")
    @Size(max = 255, message = "질문 내용은 최대 255자까지 입력 가능합니다.")
    private String qstnCn;                        // 질문 내용

    @NotBlank(message = "답변 내용은 필수 입력값입니다.")
    private String ansCn;                          // 답변 내용 (HTML 저장)
    private String ansCnTxt;                      // 답변 내용 (태그 제거)

    private Integer sortSeq;                        // 정렬 순서

    @NotBlank(message = "공개여부는 필수 입력값입니다.")
    @Pattern(regexp = "^[YN]$", message = "공개여부는 Y 또는 N만 허용됩니다.")
    private String openYn;                          // 공개여부

    @Pattern(regexp = "^[YN]$", message = "사용여부는 Y 또는 N만 허용됩니다.")
    private String useYn;                           // 사용여부

    private Integer inqCnt;                        // 조회수

    @Size(max = 10, message = "등록자 ID는 최대 10자까지 입력 가능합니다.")
    private String regId;                       // 등록자 ID

    private LocalDate regDt;                        // 등록일시

    @Size(max = 10, message = "수정자 ID는 최대 10자까지 입력 가능합니다.")
    private String mdfcnId;                       // 수정자 ID

    private LocalDate mdfcnDt;                        // 수정일시

    private String ctgryNm;
    private List<FileDTO> faqAttachList;
    private List<FileDTO> faqCkEditorAttachList;
}
