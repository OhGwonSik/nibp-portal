package egovframework.admin.admin800.mapper;

import egovframework.admin.admin800.domain.*;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

/**
 * @ClassName : Admin803Mapper.java
 * @Description : 권한 관리 Mapper
 *
 * @author : balee
 * @since  : 2025. 11. 17
 * @version : 1.0
 */
@Mapper
public interface Admin803Mapper {
    /**
     * 관리자 계정 목록 조회
     *
     * @param filter Admin803FilterDTO 검색 및 페이징 조건
     * @return List<Admin803AccountVO> 관리자 계정 목록
     */
    List<Admin803AccountVO> selectAdmin803AccountList(Admin803FilterDTO filter);

    /**
     * 사용자별 메뉴 권한 목록 조회
     *
     * @param admin803MenuDTO 사용자 정보
     * @return List<Admin803MenuDTO> 사용자 메뉴 권한 목록
     */
    List<Admin803MenuDTO> selectAdmin803MenuList(Admin803MenuDTO admin803MenuDTO);

    /**
     * 사용자 메뉴 권한 조회
     *
     * @param userOid 사용자번호
     * @param menuOid 메뉴번호
     * @return Admin803AuthDTO 사용자 메뉴 권한 정보
     */
    Admin803AuthDTO selectUserMenuAuth(@Param("userOid") Long userOid, @Param("menuOid") Long menuOid);

    /**
     * 사용자의 모든 메뉴 권한 목록 조회
     *
     * @param userOid 사용자번호
     * @return List<Admin803AuthDTO> 사용자 메뉴 권한 목록
     */
    List<Admin803AuthDTO> selectUserMenuAuthListByUserNo(@Param("userOid") Long userOid);

    /**
     * 사용자 메뉴 권한 등록
     *
     * @param authItems 사용자 메뉴 권한 정보 리스트
     * @return int
     */
    int insertUserMenuAuth(@Param("list") List<Admin803AuthDTO> authItems);

    /**
     * 사용자 메뉴 권한 수정
     *
     * @param authItems 사용자 메뉴 권한 정보 리스트
     * @return int
     */
    int updateUserMenuAuth(List<Admin803AuthDTO> authItems);

    /**
     * 개인정보취급권한 수정
     *
     * @param admin803AccountDTO 사용자 개인정보취급권한 정보
     * @return int
     */
    int updateAdmin803PrivacyAuth(Admin803AccountDTO admin803AccountDTO);

    /**
     * 타겟 유저들의 기존 메뉴 권한 삭제
     *
     * @param targetUserNos 타겟 사용자번호 목록
     * @return int 삭제된 행 수
     */
    int deleteUserMenuAuthByUserNos(@Param("targetUserNos") List<Long> targetUserNos);

    /**
     * 소스 유저의 활성 권한을 타겟 유저들에게 복사 (INSERT ... SELECT CROSS JOIN)
     *
     * @param sourceUserNo 소스 사용자번호
     * @param targetUserNos 타겟 사용자번호 목록
     * @param regId 등록자 ID
     * @return int 삽입된 행 수
     */
    int copyMenuAuthFromSource(@Param("sourceUserNo") Long sourceUserNo, @Param("targetUserNos") List<Long> targetUserNos, @Param("regId") String regId);

    /**
     * 만료된 메뉴 권한 조회 (auth_end_dt가 현재 시점 이전인 권한)
     *
     * @return List<Admin803AuthDTO> 만료된 메뉴 권한 목록
     */
    List<Admin803AuthDTO> selectExpiredMenuAuth();

    /**
     * 만료된 메뉴 권한 비활성화 (모든 YN 값을 N으로 변경)
     *
     * @return int 업데이트된 행 수
     */
    int deactivateExpiredMenuAuth();
}