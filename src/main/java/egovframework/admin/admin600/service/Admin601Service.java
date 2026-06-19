package egovframework.admin.admin600.service;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin600.domain.Admin601DetailDTO;
import egovframework.admin.admin600.domain.Admin601VO;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.io.IOException;

public interface Admin601Service {
	PageInfo<Admin601VO> selectNoticeList(EgovMap egovMap);

	Admin601DetailDTO selectNotice(Long ntcOid);

	int insertNotice(EgovMap egovMap) throws RuntimeException, IOException;

	int updateNotice(EgovMap egovMap) throws RuntimeException, IOException;

	int deleteNotice(Long ntcOid);

	void updateNoticeInqCnt(Long ntcOid);
}
