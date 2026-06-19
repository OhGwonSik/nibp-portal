package egovframework.admin.admin700.mapper;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.admin.admin700.domain.Admin702ExcelDTO;
import egovframework.admin.admin700.domain.Admin702FilterDTO;
import egovframework.admin.admin700.domain.MonthlyQnaStatisticsDataDTO;
import egovframework.admin.admin700.domain.MonthlyVisitorStatisticsDataDTO;

@Mapper
public interface Admin702Mapper {
	// QNA 데이터 조회
	public List<MonthlyQnaStatisticsDataDTO> selectMonthlyQnaStatisticsData(Admin702FilterDTO filter);
	// 방문자 데이터 조회
	public List<MonthlyVisitorStatisticsDataDTO> selectMonthlyVisitorStatisticsData(Admin702FilterDTO filter);
	// QNA 데이터 조회(엑셀)
	public List<MonthlyQnaStatisticsDataDTO> selectMonthlyQnaStatisticsDataForExcel(Admin702ExcelDTO filter);
	// 방문자 데이터 조회(엑셀)
	public List<MonthlyVisitorStatisticsDataDTO> selectMonthlyVisitorStatisticsDataForExcel(Admin702ExcelDTO filter);
}
