package egovframework.admin.admin600.mapper;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import egovframework.admin.admin600.domain.Admin601VO;

@Mapper
public interface Admin601Mapper {
	List<Admin601VO> selectNoticeList(EgovMap egovMap);

	Admin601VO selectNotice(Long ntcOid);

	int insertNotice(EgovMap egovMap);

	int updateNotice(EgovMap egovMap);

	int deleteNotice(EgovMap egovMap);

	void updateNoticeInqCnt(Long ntcOid);
}
