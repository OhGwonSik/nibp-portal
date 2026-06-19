package egovframework.admin.admin700.mapper;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.admin.admin700.domain.QnaRegisteredStatDTO;
import egovframework.admin.admin700.domain.QnaRegisteredStatFilterDTO;

@Mapper
public interface Admin704Mapper {

	public List<QnaRegisteredStatDTO> selectQnaRegisteredStat(QnaRegisteredStatFilterDTO qnaRegisteredStatFilterDTO);
}
