package egovframework.admin.admin600.domain;

import egovframework.common.file.domain.AttachedFileDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import egovframework.common.annotation.HtmlTextMaxSize;
import egovframework.common.constant.Constants;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@ToString
public class Admin601SaveDTO {
    private Long ntcOid;

    @NotBlank(message = "공지 제목은 필수 입력값입니다.")
    @Size(min = 1, max = 100, message = "공지 제목은 1자 이상 100자 이하로 입력해주세요.")
    private String ntcNm;

    @NotBlank(message = "공지 내용은 필수 입력값입니다.")
    @HtmlTextMaxSize(max = Constants.MAX_TEXT_LENGTH, maxHtml = Constants.MAX_HTML_LENGTH, message = "공지 내용은 최대 " + Constants.MAX_TEXT_LENGTH + "자까지 입력 가능합니다.")
    private String ntcCn;
    private String ntcCnTxt;

    @NotBlank(message = "게시 시작일은 필수 입력값입니다.")
    private String bgngDt;

    private String endDt;

    @Pattern(regexp = "^[YN]$", message = "상단고정 여부는 Y 또는 N만 입력 가능합니다.")
    private String upendFixYn;

    private String upendFixBgngDt;
    private String upendFixEndDt;

    @NotBlank(message = "공개 여부는 필수 입력값입니다.")
    @Pattern(regexp = "^[YN]$", message = "공개 여부는 Y 또는 N만 입력 가능합니다.")
    private String openYn;

    private String tempUploadKey;
    private String rfrncSeCd;
    private List<Long> deleteAttachNos;
    private List<AttachedFileDTO> attachedFiles;
    private List<AttachedFileDTO> editorFiles;
    private List<Long> deleteEditorAttachNos;
    private String firstFileAltText;  // 첫 번째 파일의 대체 텍스트

    public static void populateDtoFields(EgovMap target, Admin601SaveDTO dto) {
        target.put("ntcOid", dto.getNtcOid());
        target.put("ntcNm", dto.getNtcNm());
        target.put("ntcCn", dto.getNtcCn());
        target.put("ntcCnTxt", dto.getNtcCnTxt());
        target.put("bgngDt", dto.getBgngDt());
        target.put("endDt", dto.getEndDt());
        target.put("upendFixYn", dto.getUpendFixYn());
        target.put("upendFixBgngDt", dto.getUpendFixBgngDt());
        target.put("upendFixEndDt", dto.getUpendFixEndDt());
        target.put("openYn", dto.getOpenYn());
        target.put("deleteAttachNos", dto.getDeleteAttachNos());
        target.put("tempUploadKey", dto.getTempUploadKey());
        target.put("rfrncSeCd", dto.getRfrncSeCd());
        target.put("deleteEditorAttachNos", dto.getDeleteEditorAttachNos());
    }
}
