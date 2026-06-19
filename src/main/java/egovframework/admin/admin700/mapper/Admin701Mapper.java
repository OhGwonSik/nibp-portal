package egovframework.admin.admin700.mapper;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.admin.admin700.domain.Admin701ExcelDTO;
import egovframework.admin.admin700.domain.Admin701FilterDTO;
import egovframework.admin.admin700.domain.QnaDataDTO;

@Mapper
public interface Admin701Mapper {
	// QNA 데이터 조회
	public List<QnaDataDTO> selectQnaDataList(Admin701FilterDTO filter);
	public List<QnaDataDTO> selectQnaDataListForExcel(Admin701ExcelDTO params);
}
