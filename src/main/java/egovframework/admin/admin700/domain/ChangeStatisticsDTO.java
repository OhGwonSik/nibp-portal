package egovframework.admin.admin700.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChangeStatisticsDTO {
    // This DTO will hold MoM and YoY changes for relevant metrics
    private String questionCountChange;
    private String visitorChange;
}
