package egovframework.admin.admin800.mapper;

import egovframework.admin.admin800.domain.Admin801ResponseDto;
import egovframework.admin.admin800.domain.Admin801VO;
import egovframework.admin.admin800.domain.Admin801filterDto;
import egovframework.admin.admin800.domain.UnlockAccountDto;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.util.List;

/**
 * @ClassName : Admin801Mapper.java
 * @Description : 관리자 관리 관련 Mapper
 *
 * @author : balee
 * @since  : 2025. 11. 19
 * @version : 1.0
 */
@Mapper
public interface Admin801Mapper {
    /**
     * 관리자 목록 조회 (필터 적용)
     *
     * @param filter 검색 조건 및 페이징 정보
     * @return List<Admin801ResponseDto> 관리자 목록
     * @throws RuntimeException 목록 조회 중 오류 발생 시
     */
    List<Admin801ResponseDto> selectAdmin801ListWithFilter(@Param("filter") Admin801filterDto filter, @Param("aesKey") String aesKey);

    /**
     * 관리자 상세 정보 조회
     *
     * @param egovMap 검색 조건 (userOid)
     * @return Admin801VO 관리자 상세 정보
     * @throws RuntimeException 조회 중 오류 발생 시
     */
    Admin801VO selectAdmin801Detail(@Param("egovMap") EgovMap egovMap, @Param("aesKey") String aesKey);

    /**
     * 관리자 정보 등록
     *
     * @param vo 관리자 정보
     * @return int 등록된 행 수
     * @throws RuntimeException 등록 중 오류 발생 시
     */
    int insertAdmin801(Admin801VO vo);

    /**
     * 관리자 정보 수정
     *
     * @param vo 관리자 정보
     * @return int 수정된 행 수
     * @throws RuntimeException 수정 중 오류 발생 시
     */
    int updateAdmin801(Admin801VO vo);

    /**
     * 관리자 계정 잠금 해제
     *
     * @param unlockAccountDto 계정 잠금 해제 정보 (userOid, mdfcnId)
     * @return int 수정된 행 수
     * @throws RuntimeException 계정 잠금 해제 중 오류 발생 시
     */
    int unlockAccount(UnlockAccountDto unlockAccountDto);
}
