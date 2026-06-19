package egovframework.admin.admin800.service;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin810;
import egovframework.admin.admin800.domain.Admin810DTO;
import egovframework.admin.admin800.domain.Admin810FilterDTO;
import egovframework.admin.admin800.domain.Admin810VO;

/**
 * @ClassName : Admin810Service.java
 * @Description : 기관 관리 서비스 인터페이스
 *
 * @author : balee
 * @since  : 2025. 11. 11
 * @version : 1.0
 */
public interface Admin810Service {

    /**
     * 기관 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return PageInfo<Admin810VO> 페이징된 기관 목록
     * @throws RuntimeException 목록 조회 중 오류 발생 시
     */
    PageInfo<Admin810VO> selectAdmin810List(Admin810FilterDTO filter);

    /**
     * 기관 단건 조회
     * @param instOid 검색 조건 (기관번호)
     * @throws RuntimeException 조회 중 오류 발생 시
     */
    Admin810VO selectAdmin810(String instOid);

    /**
     * 기관 등록
     *
     * @param admin810 기관 정보
     * @throws RuntimeException 기관 등록 중 오류 발생 시
     */
    void insertAdmin810(Admin810 admin810);


    /**
     * 기관 수정
     *
     * @param admin810 기관 정보
     * @throws RuntimeException 기관 수정 중 오류 발생 시
     */
    void updateAdmin810(Admin810 admin810);

    /**
     * 기관 삭제
     *
     * @param admin810DTO 기관 정보
     * @throws RuntimeException 기관 삭제 중 오류 발생 시
     */
    void deleteAdmin810(Admin810DTO admin810DTO);
}
