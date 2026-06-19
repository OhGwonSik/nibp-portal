package egovframework.admin.admin800.service;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.*;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

/**
 * @ClassName : Admin801Service.java
 * @Description : 관리자 관리 서비스 인터페이스
 *
 * @author : balee
 * @since  : 2025. 11. 19
 * @version : 1.0
 */
public interface Admin801Service {
    /**
     * 관리자 목록 조회 (필터 적용)
     *
     * @param filter 검색 조건 및 페이징 정보
     * @return PageInfo<Admin801ResponseDto> 페이징된 관리자 목록
     * @throws RuntimeException 목록 조회 중 오류 발생 시
     */
    PageInfo<Admin801ResponseDto> selectAdmin801ListWithFilter(Admin801filterDto filter);

    /**
     * 관리자 상세 정보 조회
     *
     * @param egovMap 검색 조건 (userOid)
     * @return Admin801VO 관리자 상세 정보
     * @throws RuntimeException 조회 중 오류 발생 시
     */
    Admin801VO selectAdmin801Detail(EgovMap egovMap);

    /**
     * 관리자 정보 등록/수정 (Upsert)
     * userNo가 존재하면 수정, 없으면 등록
     *
     * @param dto 관리자 정보
     * @return int 처리된 행 수
     * @throws RuntimeException 등록/수정 중 오류 발생 시
     */
    int upsertAdmin801(Admin801DTO dto);

    /**
     * 관리자 계정 잠금 해제
     *
     * @param dto 계정 잠금 해제 정보
     * @return int 처리된 행 수
     * @throws RuntimeException 계정 잠금 해제 중 오류 발생 시
     */
    int unlockAccount(UnlockAccountDto dto);
}
