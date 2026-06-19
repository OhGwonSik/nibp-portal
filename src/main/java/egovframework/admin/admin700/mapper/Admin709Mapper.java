package egovframework.admin.admin700.mapper;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.admin.admin700.domain.Admin709FilterDTO;
import egovframework.admin.admin700.domain.VisitorStatisticsDTO;
import egovframework.admin.admin700.domain.InquiryStatisticsDTO;
import egovframework.admin.admin700.domain.ResponseStatisticsDTO;

@Mapper
public interface Admin709Mapper {
	List<VisitorStatisticsDTO> selectVisitorStatistics(Admin709FilterDTO filter);
	List<InquiryStatisticsDTO> selectInquiryStatistics(Admin709FilterDTO filter);
	List<ResponseStatisticsDTO> selectResponseStatistics(Admin709FilterDTO filter);
}
