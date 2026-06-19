package egovframework.admin.admin700.domain;

import java.time.Year;
import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin709FilterDTO {
    @DateTimeFormat(pattern = "yyyy-dd")
    private YearMonth targetDate;
}
