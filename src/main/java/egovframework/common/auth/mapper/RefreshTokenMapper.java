package egovframework.common.auth.mapper;

import egovframework.common.auth.domain.RefreshToken;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * @ClassName : RefreshTokenMapper.java
 * @Description : Refresh Token Mapper
 *
 * @author : tspark
 * @since  : 2025. 11. 04
 * @version : 1.0
 */
@Mapper
public interface RefreshTokenMapper {

    /**
     * Refresh Token 저장
     * @param refreshToken Refresh Token 정보
     * @return int 저장 결과
     */
    int insertRefreshToken(RefreshToken refreshToken);

    /**
     * Token ID로 Refresh Token 조회
     * @param tokenId Token ID
     * @return RefreshToken
     */
    RefreshToken selectRefreshTokenByTokenId(String tokenId);

    /**
     * Token 값으로 Refresh Token 조회
     * @param token Token 값
     * @return RefreshToken
     */
    RefreshToken selectRefreshTokenByToken(String token);

    /**
     * 마지막 사용 일시 업데이트
     * @param tokenId Token ID
     * @return int 업데이트 결과
     */
    int updateLastUsedDt(String tokenId);

    /**
     * Refresh Token 폐기
     * @param tokenId Token ID
     * @return int 폐기 결과
     */
    int revokeRefreshToken(String tokenId);

    /**
     * 사용자의 모든 Refresh Token 폐기
     * @param userId 사용자 ID
     * @return int 폐기 결과
     */
    int revokeAllTokensByUserId(String userId);

    /**
     * 만료된 Refresh Token 삭제
     * @return int 삭제된 행 수
     */
    int deleteExpiredTokens();
}
