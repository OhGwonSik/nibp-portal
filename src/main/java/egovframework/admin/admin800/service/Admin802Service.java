package egovframework.admin.admin800.service;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.*;

/**
 * @ClassName : Admin802Service.java
 * @Description : 그룹코드 관리 서비스 인터페이스
 *
 * @author : balee
 * @since  : 2025. 11. 18
 * @version : 1.0
 */
public interface Admin802Service {

    /**
     * 그룹코드 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return PageInfo<GroupCodeResponseDTO> 페이징된 그룹코드 목록
     * @throws RuntimeException 목록 조회 중 오류 발생 시
     */
    PageInfo<GroupCodeResponseDTO> selectGroupcodeWithFilter(GroupCodeFilterDTO filter);

    /**
     * 그룹코드 등록
     * @param groupCode 그룹코드 정보
     * @return int 등록된 행 수
     * @throws RuntimeException 그룹코드 등록 중 오류 발생 시
     */
    int insertAdmin802Group(GroupCode groupCode);

    /**
     * 그룹코드 수정
     * @param groupCode 그룹코드 정보
     * @return int 수정된 행 수
     * @throws RuntimeException 그룹코드 수정 중 오류 발생 시
     */
    int updateAdmin802Group(GroupCode groupCode);

    /**
     * 코드 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return PageInfo<CodeResponseDTO> 페이징된 코드 목록
     * @throws RuntimeException 목록 조회 중 오류 발생 시
     */
    PageInfo<CodeResponseDTO> selectCodeWithFilter(CodeFilterDTO filter);

    /**
     * 코드 등록
     * @param code 코드 정보
     * @return int 등록된 행 수
     * @throws RuntimeException 코드 등록 중 오류 발생 시
     */
    int insertAdmin802Code(Code code);

    /**
     * 코드 수정
     * @param code 코드 정보
     * @return int 수정된 행 수
     * @throws RuntimeException 코드 수정 중 오류 발생 시
     */
    int updateAdmin802Code(Code code);
}