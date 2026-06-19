package egovframework.admin.admin700.mapper;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.admin.admin700.domain.QnaAnsweredStatDTO;
import egovframework.admin.admin700.domain.QnaAnsweredStatFilterDTO;

@Mapper
public interface Admin705Mapper {

	public List<QnaAnsweredStatDTO> selectQnaAnsweredStat(QnaAnsweredStatFilterDTO qnaAnsweredStatFilterDTO);
}
