package egovframework.admin.admin700.domain;

import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin702ExcelDTO {
    @DateTimeFormat(pattern = "yyyy-MM")
    private YearMonth toDate;
    private String reason;
}
