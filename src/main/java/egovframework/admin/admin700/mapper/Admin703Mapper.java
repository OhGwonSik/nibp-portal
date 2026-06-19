package egovframework.admin.admin700.mapper;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.admin.admin700.domain.QnaMonthlyResponseStatDTO;
import egovframework.admin.admin700.domain.QnaMonthlyResponseStatFilterDTO;

@Mapper
public interface Admin703Mapper {

	public List<QnaMonthlyResponseStatDTO> selectQnaMonthlyResponseStat(QnaMonthlyResponseStatFilterDTO qnaMonthlyResponseStatFilterDTO);
}
