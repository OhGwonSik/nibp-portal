package egovframework.admin.admin600.mapper;

import egovframework.admin.admin600.domain.*;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.util.List;

/**
 * @ClassName : Admin617Mapper.java
 * @Description : 카드뉴스 관리 관련 Mapper
 *
 * @author : balee
 * @since  : 2025. 11. 27
 * @version : 1.0
 */
@Mapper
public interface Admin617Mapper {

	/**
	 * 카드뉴스 목록 조회
	 * @param filter 검색 및 페이징 조건
	 * @return List<Admin617VO> 카드뉴스 목록
	 */
	List<Admin617VO> selectAdmin617List(Admin617FilterDTO filter);

	/**
	 * 카드뉴스 단건 조회
	 * @param cardNewsOid 검색조건(카드뉴스번호)
	 * @return Admin617VO 카드뉴스
	 */
	Admin617VO selectAdmin617(Long cardNewsOid);

	/**
	 * 카드뉴스 등록
	 * @param admin617DTO 카드뉴스 정보
	 */
	int insertAdmin617(Admin617DTO admin617DTO);

	/**
	 * 카드뉴스 수정
	 * @param admin617DTO 카드뉴스 정보
	 */
	int updateAdmin617(Admin617DTO admin617DTO);

	/**
	 * 카드뉴스 삭제
	 * @param admin617DeleteDTO 카드뉴스 정보
	 */
	int deleteAdmin617(Admin617DeleteDTO admin617DeleteDTO);

	/**
	 * 카드뉴스 엑셀 다운로드용 목록 조회
	 * @param cond 검색 조건
	 * @return List<Admin617ExcelDTO> 카드뉴스 목록
	 */
	List<Admin617ExcelDTO> selectAdmin617ExcelList(EgovMap cond);

	/**
	 * 카드뉴스 조회수 증가
	 * @param cardNewsOid 카드뉴스번호
	 */
	int updateAdmin617HitCnt(Long cardNewsOid);
}
