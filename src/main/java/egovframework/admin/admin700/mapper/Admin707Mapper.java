package egovframework.admin.admin700.mapper;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.admin.admin700.domain.QnaCategoryStatDTO;
import egovframework.admin.admin700.domain.QnaCategoryStatFilterDTO;

@Mapper
public interface Admin707Mapper {

	public List<QnaCategoryStatDTO> selectQnaCategoryStat(QnaCategoryStatFilterDTO qnaCategoryStatFilterDTO);
}
