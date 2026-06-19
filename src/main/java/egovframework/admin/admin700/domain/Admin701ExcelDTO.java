package egovframework.admin.admin700.domain;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin701ExcelDTO {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromQuestionDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toQuestionDate;
    private String reason;
}
