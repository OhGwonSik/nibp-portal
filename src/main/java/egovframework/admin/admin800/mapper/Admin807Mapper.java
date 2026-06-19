package egovframework.admin.admin800.mapper;

import egovframework.admin.admin800.domain.Admin807DTO;
import egovframework.admin.admin800.domain.Admin807DeleteDTO;
import egovframework.admin.admin800.domain.Admin807FilterDTO;
import egovframework.admin.admin800.domain.Admin807VO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

/**
 * @ClassName : Admin807Mapper.java
 * @Description : 게시판 관리 Mapper
 *
 * @author : 
 * @since  : 2025. 11. 14
 * @version : 1.0
 */
@Mapper
public interface Admin807Mapper {
	/**
	 * 게시판 목록 조회
	 * @return List<Admin807VO> 게시판 목록
	 */
	List<Admin807VO> selectBoardList(Admin807FilterDTO filter);

	/**
	 * 게시판 등록
	 * @return int 등록 건수
	 */
	int insertBoard(Admin807DTO admin807DTO);

	/**
	 * 게시판 수정
	 * @return int 수정 건수
	 */
	int updateBoard(Admin807DTO admin807DTO);

	/**
	 * 게시판 삭제
	 * @return int 삭제 건수
	 */
	int deleteBoard(Admin807DeleteDTO admin807DeleteDTO);
}
