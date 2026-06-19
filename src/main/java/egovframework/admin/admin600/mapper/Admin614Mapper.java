package egovframework.admin.admin600.mapper;

import egovframework.admin.admin600.domain.*;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.util.List;

/**
 * @ClassName : Admin614Mapper.java
 * @Description : 안내(팝업) 관리 관련 Mapper
 *
 * @author : balee
 * @since  : 2025. 11. 24
 * @version : 1.0
 */
@Mapper
public interface Admin614Mapper {

	/**
	 * 안내(팝업) 목록 조회
	 * @param filter 검색 및 페이징 조건
	 * @return List<Admin614VO> 안내(팝업) 목록
	 */
	List<Admin614VO> selectAdmin614List(Admin614FilterDTO filter);

	/**
	 * 안내(팝업) 단건 조회
	 * @param popupOid 검색조건(팝업번호)
	 * @return 안내(팝업)
	 */
	Admin614VO selectAdmin614(String popupOid);

	/**
	 * 안내(팝업) 등록
	 * @param admin614DTO 안내(팝업) 정보
	 * @return
	 */
	int insertAdmin614(Admin614DTO admin614DTO);

	/**
	 * 안내(팝업) 수정
	 * @param admin614DTO 안내(팝업) 정보
	 * @return
	 */
	int updateAdmin614(Admin614DTO admin614DTO);

	/**
	 * 안내(팝업) 삭제
	 * @param admin614DeleteDTO 안내(팝업) 정보
	 * @return
	 */
	int deleteAdmin614(Admin614DeleteDTO admin614DeleteDTO);

	/**
	 * 안내(팝업) 엑셀 다운로드용 목록 조회
	 * @param cond 검색 조건
	 * @return List<Admin614ExcelDTO> 안내(팝업) 목록
	 */
	List<Admin614ExcelDTO> selectAdmin614ExcelList(EgovMap cond);
}
