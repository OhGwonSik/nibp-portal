package egovframework.common.security;

/**
 * @ClassName : SecurityConstants.java
 * @Description : 보안 관련 상수 정의
 *
 * @author : tspark
 * @since  : 2025. 11. 18
 * @version : 1.0
 */
public class SecurityConstants {

    // 인증이 필요 없는 공개 인증 API
    public static final String[] PUBLIC_AUTH_URLS = {
        "/auth/login",
        "/auth/admin/login",
        "/auth/refresh",
        "/auth/join",
        "/page/**",
        "/files/**",
        "/temp/**",
        "/xe/**"
    };

    // 인증 API 및 로그인 페이지
    public static final String[] COMMON_AUTH_URLS = {
        "/auth/**",
        "/login",
        "/signup/**",
        "/find_id_pwd",
        "/reset_pwd",
        "/admin/login",
        "/admin/find-id-pwd",
    };

    // 사용자 API
    public static final String[] USER_API_URLS = {
        "/api/user/**"
    };

    // 관리자 API
    public static final String[] ADMIN_API_URLS = {
        "/api/admin/**"
    };

    // 공개 API
    public static final String[] PUBLIC_API_URLS = {
        "/api/common/**"
    };

    // Rate Limiting에서 제외할 API 경로
    public static final String[] RATE_LIMIT_EXCLUDED_PATTERNS = {
        "/api/admin/menu/my_menu",          // 관리자페이지 메뉴
        "/api/common/menu",                 // 일반페이지 메뉴
        "/api/upload/chunk",                // chunk upload(구버전)
        "/api/common/file/chunk",           // 파일 청크 업로드
        "/api/common/file/tmp/**",          // 임시 파일 업로드 (CKEditor 이미지 등)
        "/api/common/file/cancel",          // 업로드 취소
        "/api/common/file/chunk/temp-file"  // 임시 파일 삭제
    };

    // 메인 페이지
    public static final String[] MAIN_URLS = {
        "/",
        "/main"
    };

    public static final String[] BOARD_URLS = {
        "/board/**"
    };

    // 포탈 리소스
    public static final String[] PORTAL_RESOURCES = {
        "/portal/css/**",
        "/portal/js/**",
        "/portal/img/**",
        "/portal/images/**"
    };

    // 관리자 리소스
    public static final String[] ADMIN_RESOURCES = {
        "/admin/css/**",
        "/admin/js/**",
        "/admin/img/**",
        "/admin/images/**",
        "/admin/fonts/**",
        "/admin/font/**"
    };

    // 공통 리소스
    public static final String[] COMMON_RESOURCES = {
        "/common/css/**",
        "/common/js/**",
        "/common/img/**",
        "/common/images/**",
        "/common/font/**",
        "/common/fonts/**",
        "/common/assets/**",
        "/common/libs/**"
    };

    // 모든 리소스 통합
    public static final String[] ALL_RESOURCES = mergeResourceArrays();

    /**
     * 모든 리소스 배열 병합
     */
    private static String[] mergeResourceArrays() {
        int totalLength = PORTAL_RESOURCES.length + ADMIN_RESOURCES.length + COMMON_RESOURCES.length;
        String[] allResources = new String[totalLength];

        int index = 0;
        System.arraycopy(PORTAL_RESOURCES, 0, allResources, index, PORTAL_RESOURCES.length);
        index += PORTAL_RESOURCES.length;

        System.arraycopy(ADMIN_RESOURCES, 0, allResources, index, ADMIN_RESOURCES.length);
        index += ADMIN_RESOURCES.length;

        System.arraycopy(COMMON_RESOURCES, 0, allResources, index, COMMON_RESOURCES.length);

        return allResources;
    }
}
