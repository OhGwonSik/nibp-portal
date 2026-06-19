package egovframework.portal.opinion.domain;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import egovframework.common.annotation.HtmlTextMaxSize;
import egovframework.common.constant.Constants;
import egovframework.common.file.domain.FileDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PublicDataOpinionSaveDTO {
	
	private Long pblDataOpnnOid;

	@NotBlank(message = "제목은 필수 입력입니다.")
    @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
    private String pblDataOpnnTtl;

	@NotBlank(message = "내용은 필수 입력입니다.")
	@HtmlTextMaxSize(max = Constants.MAX_TEXT_LENGTH, maxHtml = Constants.MAX_HTML_LENGTH, message = "내용은 최대 " + Constants.MAX_TEXT_LENGTH + "자까지 입력 가능합니다.")
    private String pblDataOpnnCn;

	private String pblDataOpnnCnTxt;
	
	private List<FileDTO> editorFiles;
}
