package egovframework.admin.admin600.mapper;

import egovframework.admin.admin600.domain.*;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.util.List;

/**
 * @ClassName : Admin618Mapper.java
 * @Description : 팝업존 관리 관련 Mapper
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
@Mapper
public interface Admin618Mapper {

	/**
	 * 팝업존 목록 조회
	 * @param filter 검색 및 페이징 조건
	 * @return List<Admin618VO> 팝업존 목록
	 */
	List<Admin618VO> selectAdmin618List(Admin618FilterDTO filter);

	/**
	 * 팝업존 단건 조회
	 * @param popupZoneOid 검색조건(팝업존번호)
	 * @return Admin618VO 팝업존
	 */
	Admin618VO selectAdmin618(Long popupZoneOid);

	/**
	 * 팝업존 등록
	 * @param admin618DTO 팝업존 정보
	 */
	int insertAdmin618(Admin618DTO admin618DTO);

	/**
	 * 팝업존 수정
	 * @param admin618DTO 팝업존 정보
	 */
	int updateAdmin618(Admin618DTO admin618DTO);

	/**
	 * 팝업존 삭제
	 * @param admin618DeleteDTO 팝업존 정보
	 */
	int deleteAdmin618(Admin618DeleteDTO admin618DeleteDTO);

	/**
	 * 팝업존 엑셀 다운로드용 목록 조회
	 * @param cond 검색 조건
	 * @return List<Admin618ExcelDTO> 팝업존 목록
	 */
	List<Admin618ExcelDTO> selectAdmin618ExcelList(EgovMap cond);

	/**
	 * 팝업존 조회수 증가
	 * @param popupZoneOid 팝업존번호
	 */
	int updateAdmin618HitCnt(Long popupZoneOid);
}
