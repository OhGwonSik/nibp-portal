package egovframework.admin.admin700.service;

import egovframework.admin.admin700.domain.Admin709FilterDTO;
import egovframework.admin.admin700.domain.EngagementStatisticsDTO;

public interface Admin709Service {
    EngagementStatisticsDTO selectEngagementStatistics(Admin709FilterDTO filter);
}
