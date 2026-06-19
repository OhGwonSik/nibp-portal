package egovframework.admin.admin800.mapper;

import egovframework.admin.admin800.domain.Admin810;
import egovframework.admin.admin800.domain.Admin810DTO;
import egovframework.admin.admin800.domain.Admin810FilterDTO;
import egovframework.admin.admin800.domain.Admin810VO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

/**
 * @ClassName : Admin810Mapper.java
 * @Description : 기관 관리 관련 Mapper
 *
 * @author : balee
 * @since  : 2025. 11. 11
 * @version : 1.0
 */
@Mapper
public interface Admin810Mapper {

	/**
	 * 기관 목록 조회
	 * @param filter 검색 및 페이징 조건
	 * @return List<Admin810VO> 기관 목록
	 */
	List<Admin810VO> selectAdmin810List(Admin810FilterDTO filter);

	/**
	 * 기관 단건 조회
	 * @param instOid 검색조건(기관번호)
	 * @return List<Admin810VO> 기관 목록
	 */
	Admin810VO selectAdmin810(String instOid);

	/**
	 * 사업자등록번호로 기관 조회
	 * @param bizRegNo 사업자등록번호
	 * @return Admin810VO 기관 정보
	 */
	Admin810VO selectByBizRegNo(String bizRegNo);

	/**
	 * 기관 등록
	 * @param admin810 기관 정보
	 * @return
	 */
	int insertAdmin810(Admin810 admin810);

	/**
	 * 기관 수정
	 * @param admin810 기관 정보
	 * @return
	 */
	int updateAdmin810(Admin810 admin810);

	/**
	 * 기관 삭제
	 * @param admin810DTO 기관 정보
	 * @return
	 */
	int deleteAdmin810(Admin810DTO admin810DTO);
}
