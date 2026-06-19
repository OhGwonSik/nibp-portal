package egovframework.admin.admin700.domain;

import java.time.Year;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin708ExcelDTO {
    @DateTimeFormat(pattern = "yyyy")
    private Year toSatisfiyYear;
    private String targetYear;
    private String reason;
}
