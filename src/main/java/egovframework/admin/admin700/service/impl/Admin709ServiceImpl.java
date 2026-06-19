package egovframework.admin.admin700.service.impl;

import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import egovframework.admin.admin700.domain.Admin709FilterDTO;
import egovframework.admin.admin700.domain.EngagementStatisticsDTO;
import egovframework.admin.admin700.domain.InquiryStatisticsDTO;
import egovframework.admin.admin700.domain.ResponseStatisticsDTO;
import egovframework.admin.admin700.domain.VisitorStatisticsDTO;
import egovframework.admin.admin700.mapper.Admin709Mapper;
import egovframework.admin.admin700.service.Admin709Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Admin709ServiceImpl extends EgovAbstractServiceImpl implements Admin709Service {
    private final Admin709Mapper admin709Mapper;

    @Override
    public EngagementStatisticsDTO selectEngagementStatistics(Admin709FilterDTO filter) {
        List<VisitorStatisticsDTO> visitorStatistics = admin709Mapper.selectVisitorStatistics(filter);
        List<InquiryStatisticsDTO> inquiryStatistics = admin709Mapper.selectInquiryStatistics(filter);
        List<ResponseStatisticsDTO> responseStatistics = admin709Mapper.selectResponseStatistics(filter);

        EngagementStatisticsDTO dto = EngagementStatisticsDTO.builder()
            .visitorStatistics(visitorStatistics)
            .inquiryStatistics(inquiryStatistics)
            .responseStatistics(responseStatistics)
            .build();
        
        return dto;
    }
}
