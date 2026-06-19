package egovframework.common.excel.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ExcelPageConfigDTO {

	private String title;
	
	private List<ExcelColumnDTO> header;
}
