package egovframework.admin.admin800.service;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.*;

import java.util.List;

/**
 * @ClassName : Admin803Service.java
 * @Description : 권한 관리 서비스 인터페이스
 *
 * @author : balee
 * @since  : 2025. 11. 17
 * @version : 1.0
 */
public interface Admin803Service {
    /**
     * 관리자 계정 목록 조회
     *
     * @param filter 검색 및 페이징 조건
     * @return PageInfo<Admin803AccountVO> 페이징된 관리자 계정 목록
     */
    PageInfo<Admin803AccountVO> selectAdmin803AccountList(Admin803FilterDTO filter);

    /**
     * 사용자별 메뉴 권한 목록 조회
     *
     * @param admin803MenuDTO 사용자 정보
     * @return List<Admin803MenuDTO> 메뉴 권한 목록
     */
    List<Admin803MenuDTO> selectAdmin803MenuList(Admin803MenuDTO admin803MenuDTO);

    /**
     * 사용자 메뉴 권한 등록/수정
     *
     * @param authList 메뉴 권한 정보 리스트
     * @param changeReason 변경 사유
     * @throws RuntimeException 메뉴 권한 등록/수정 중 오류 발생 시
     */
    void upsertAdmin803MenuAuth(List<Admin803AuthDTO> authList, String changeReason);

    /**
     * 권한 복사 (소스 유저 → 타겟 유저 다수)
     *
     * @param requestDTO 소스 유저, 타겟 유저 목록, 변경 사유
     * @param chnrgUserId 변경자 ID
     */
    void copyMenuAuth(Admin803CopyAuthRequestDTO requestDTO, String chnrgUserId);

    /**
     * 개인정보취급권한 수정
     *
     * @param admin803AccountDTO 사용자 개인정보취급권한 정보
     */
    void updateAdmin803PrivacyAuth(Admin803AccountDTO admin803AccountDTO);
}