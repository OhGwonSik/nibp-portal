package egovframework.admin.mypage.mapper;

import egovframework.admin.mypage.domain.MypageUpdateDto;
import egovframework.admin.mypage.domain.MypageUpdatePwdDto;
import egovframework.common.auth.domain.BaseUser;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * @ClassName : AuthMapper.java
 * @Description : 인증 관련 Mapper
 *
 * @author : tspark
 * @since  : 2025. 11. 04
 * @version : 1.0
 */
@Mapper
public interface MypageMapper {

    /**
     * 관리자 ID로 사용자 정보 조회
     * @param userId 사용자 ID
     * @return BaseUser 사용자 정보
     */
    BaseUser selectAdminUserByUserId(@Param("userId") String userId, @Param("secretKey") String secretKey);

    /**
     * 관리자 정보 수정
     * @param mypageUpdateDto 관리자 정보
     * @return int 업데이트 결과
     */
    int updateAdminMe(@Param("request") MypageUpdateDto mypageUpdateDto, @Param("principal") BaseUser principal);

    /**
     * 관리자 비밀번호 수정
     * @param mypageUpdatePwdDto 관리자 정보
     * @return int 업데이트 결과
     */
    int updateAdminMePwd(@Param("request") MypageUpdatePwdDto mypageUpdatePwdDto, @Param("principal") BaseUser principal);
}