package egovframework.admin.admin700.mapper;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.admin.admin700.domain.QnaAvgTimeStatDTO;
import egovframework.admin.admin700.domain.QnaAvgTimeStatFilterDTO;

@Mapper
public interface Admin706Mapper {

	public List<QnaAvgTimeStatDTO> selectQnaAvgTimeStat(QnaAvgTimeStatFilterDTO qnaAvgTimeStatFilterDTO);
}
