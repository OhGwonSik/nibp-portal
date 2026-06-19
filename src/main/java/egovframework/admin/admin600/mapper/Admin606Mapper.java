package egovframework.admin.admin600.mapper;

import egovframework.admin.admin600.domain.*;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.util.List;

/**
 * @ClassName : Admin606Mapper.java
 * @Description : FAQ 관리 관련 Mapper
 *
 * @author : balee
 * @since  : 2025. 11. 25
 * @version : 1.0
 */
@Mapper
public interface Admin606Mapper {

	/**
	 * FAQ 목록 조회
	 * @param filter 검색 및 페이징 조건
	 * @return List<Admin606DetailVO> FAQ 목록
	 */
	List<Admin606VO> selectAdmin606List(Admin606FilterDTO filter);

	/**
	 * FAQ 카테고리 목록 조회
	 * @return List<Admin606CategoryVO> FAQ 카테고리 목록
	 */
	List<Admin606CategoryVO> selectAdmin606CategoryList();

	/**
	 * FAQ 단건 조회
	 * @param faqDtlOid 검색조건(FAQ 번호)
	 * @return List<Admin810VO> FAQ 목록
	 */
	Admin606VO selectAdmin606(Long faqDtlOid);

	/**
	 * FAQ 등록
	 * @param admin606DetailDTO FAQ 정보
	 * @return
	 */
	int insertAdmin606(Admin606DetailDTO admin606DetailDTO);

	/**
	 * FAQ 수정
	 * @param admin606DetailDTO FAQ 정보
	 * @return
	 */
	int updateAdmin606(Admin606DetailDTO admin606DetailDTO);

	/**
	 * FAQ 삭제
	 * @param admin606DeleteDTO FAQ 정보
	 * @return
	 */
	int deleteAdmin606(Admin606DeleteDTO admin606DeleteDTO);

	/**
	 * FAQ 조회수 증가
	 * @param faqDtlOid FAQ 번호
	 * @return
	 */
	int updateAdmin606inqCnt(Long faqDtlOid);

	/**
	 * FAQ 엑셀 다운로드용 목록 조회
	 * @param cond 검색 조건
	 * @return List<Admin606ExcelDTO> FAQ 목록
	 */
	List<Admin606ExcelDTO> selectAdmin606ExcelList(EgovMap cond);

	int insertFaqData(EgovMap egovMap);
	int updateFaqData(EgovMap egovMap);
}
