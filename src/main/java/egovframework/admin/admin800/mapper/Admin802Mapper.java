package egovframework.admin.admin800.mapper;

import egovframework.admin.admin800.domain.*;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

/**
 * @ClassName : Admin802Mapper.java
 * @Description : 공통코드 관리 관련 Mapper
 *
 * @author : balee
 * @since  : 2025. 11. 18
 * @version : 1.0
 */
@Mapper
public interface Admin802Mapper {

	/**
	 * 그룹코드 목록 조회
	 * @param filter 검색 및 페이징 조건
	 * @return List<GroupCodeResponseDTO> 그룹코드 목록
	 */
	List<GroupCodeResponseDTO> selectGroupcodeWithFilter(GroupCodeFilterDTO filter);

	/**
	 * 코드 목록 조회
	 * @param filter 검색 및 페이징 조건
	 * @return List<CodeResponseDTO> 코드 목록
	 */
	List<CodeResponseDTO> selectCodeWithFilter(CodeFilterDTO filter);

	/**
	 * 그룹코드로 그룹코드 테이블 조회
	 * @param grpCd 그룹코드
	 * @return GroupCode 그룹코드 정보
	 */
	GroupCode selectByGrpCd(String grpCd);

	/**
	 * 그룹코드 등록
	 * @param groupCode 그룹코드 정보
	 * @return int 등록 결과
	 */
	int insertAdmin802Group(GroupCode groupCode);
	
	/**
	 * 중복 코드 확인
	 * @param code 코드 정보
	 * @return int 등록 결과
	 */
	int selectByCd(Code code);

	/**
	 * 코드 등록
	 * @param code 코드 정보
	 * @return int 등록 결과
	 */
	int insertAdmin802Code(Code code);

	/**
	 * 그룹코드 수정
	 * @param parameterObject 그룹코드 정보
	 * @return int 수정 결과
	 */
	int updateAdmin802Group(Object parameterObject);

	/**
	 * 코드 수정
	 * @param parameterObject 코드 정보
	 * @return int 수정 결과
	 */
	int updateAdmin802Code(Object parameterObject);


	int deleteAdmin802(Object parameterObject);
}