package egovframework.common.audit.dto;

import lombok.Builder;

/**
 * 로그아웃 정보 업데이트를 위한 DTO
 * @param userId 사용자 ID
 * @param userOid 사용자 번호
 * @param loginSsnId 로그인 세션 ID
 */
@Builder
public record UpdateLogoutInfoDto(
    String userId,
    Long userOid,
    String loginSsnId
) {
}
