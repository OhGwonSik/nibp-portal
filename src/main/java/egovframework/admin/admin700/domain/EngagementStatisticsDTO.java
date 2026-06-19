package egovframework.admin.admin700.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngagementStatisticsDTO {
    private List<?> visitorStatistics;
    private List<?> inquiryStatistics;
    private List<?> responseStatistics;
}
