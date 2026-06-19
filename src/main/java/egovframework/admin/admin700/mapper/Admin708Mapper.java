package egovframework.admin.admin700.mapper;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.admin.admin700.domain.Admin708ExcelDTO;
import egovframework.admin.admin700.domain.Admin708FilterDTO;
import egovframework.admin.admin700.domain.QnaSatisfactionStatistcsDTO;

@Mapper
public interface Admin708Mapper {
	List<QnaSatisfactionStatistcsDTO> selectQnaSatisfactionStatisticsData(Admin708FilterDTO filter);
	List<QnaSatisfactionStatistcsDTO> selectQnaSatisfactionStatisticsDataForExcel(Admin708ExcelDTO params);
}
