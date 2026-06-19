package egovframework.common.excel.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ExcelExportResult {
    private String fileName;
    private byte[] bytes;
}
