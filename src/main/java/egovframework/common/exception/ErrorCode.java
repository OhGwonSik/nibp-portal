package egovframework.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
/**
 * @ClassName : ErrorCode.java
 * @Description : 에러 코드
 *
 * @author : tspark
 * @since  : 2025. 10. 29
 * @version : 1.0
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일              수정자               수정내용
 *  -------------  ------------   ---------------------
 *   2025. 10. 29    tspark               최초 생성
 * </pre>
 *
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 인증 관련 에러 (AUTH_xxx)
    AUTHENTICATION_FAILED("AUTH_001", "이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("AUTH_002", "로그인 시도 횟수를 초과하여 계정이 잠겼습니다. 비밀번호 초기화를 진행해주세요.", HttpStatus.LOCKED),
    ACCOUNT_DISABLED("AUTH_003", "사용 중지된 계정입니다.", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED("AUTH_004", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("AUTH_005", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKE_FAILED("AUTH_006", "토큰 해제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    LOGOUT_FAILED("AUTH_007", "로그아웃에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 사용자 관리 에러 (USER_xxx)
    USER_NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("USER_002", "이미 존재하는 사용자입니다.", HttpStatus.CONFLICT),
    USER_CREATE_FAILED("USER_003", "사용자 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_UPDATE_FAILED("USER_004", "사용자 정보 수정에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_DELETE_FAILED("USER_005", "사용자 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PASSWORD_CHANGE_FAILED("USER_006", "비밀번호 변경에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_UNLOCK_FAILED("USER_007", "계정 잠금 해제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 권한 관리 에러 (ROLE_xxx)
    ROLE_NOT_FOUND("ROLE_001", "권한을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ROLE_ASSIGN_FAILED("ROLE_002", "권한 할당에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INSUFFICIENT_PERMISSIONS("ROLE_003", "권한이 부족합니다.", HttpStatus.FORBIDDEN),
    ROLE_ALREADY_EXISTS("ROLE_004", "이미 존재하는 권한입니다.", HttpStatus.CONFLICT),
    ROLE_CREATE_FAILED("ROLE_005", "권한 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 접근 거부 에러
    ACCESS_DENIED("ROLE_009", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 보안 관련 에러 (SECURITY_xxx)
    DECRYPTION_FAILED("SECURITY_001", "데이터 복호화에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    ROLE_UPDATE_FAILED("ROLE_006", "권한 수정에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_DELETE_FAILED("ROLE_007", "권한 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_IN_USE("ROLE_008", "사용 중인 권한은 삭제할 수 없습니다.", HttpStatus.CONFLICT),

    // 검증 에러 (VALID_xxx)
    VALIDATION_FAILED("VALID_001", "유효성 검증에 실패했습니다.", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_FORMAT("VALID_002", "잘못된 요청 형식입니다.", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD_MISSING("VALID_003", "필수 항목이 누락되었습니다.", HttpStatus.BAD_REQUEST),

    // 서버 에러 (SERVER_xxx)
    INTERNAL_SERVER_ERROR("SERVER_001", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR("SERVER_002", "데이터베이스 작업 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_SERVICE_ERROR("SERVER_003", "외부 서비스 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),

    // 기타 에러 (COMMON_xxx)
    RESOURCE_NOT_FOUND("COMMON_001", "요청하신 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    OPERATION_NOT_ALLOWED("COMMON_002", "허용되지 않은 작업입니다.", HttpStatus.FORBIDDEN),
    RATE_LIMIT_EXCEEDED("COMMON_003", "요청 한도가 초과되었습니다.", HttpStatus.TOO_MANY_REQUESTS),
    DUPLICATE_RESOURCE("COMMON_004", "중복된 리소스가 존재합니다.", HttpStatus.CONFLICT),

    // 파일 다운로드 에러(FILE_xxx)
    FILE_DOWNLOAD_FAILED("FILE_001", "파일 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND("FILE_002", "파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 수강 신청 관련 에러
    COURSE_REQUEST_EMPTY("COURSE_001","처리할 수강 신청 대상이 없습니다.",HttpStatus.BAD_REQUEST),
    COURSE_NO_MISSING("COURSE_002", "교육 정보가 없습니다.", HttpStatus.BAD_REQUEST),
    COURSE_NO_MISMATCH("COURSE_003", "동일한 교육에 대해서만 일괄 처리가 가능합니다.", HttpStatus.BAD_REQUEST),
    COURSE_NOT_RECRUITING("COURSE_004", "모집 중인 교육만 승인 처리가 가능합니다.", HttpStatus.FORBIDDEN),
    COURSE_CAPACITY_EXCEEDED("COURSE_005", "교육 정원을 초과하여 승인할 수 없습니다.", HttpStatus.BAD_REQUEST),
    COURSE_APPROVAL_FAILED("COURSE_006", "수강 승인 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    COURSE_ALREADY_APPROVED("COURSE_007","이미 승인된 사용자가 포함되어 있습니다.",HttpStatus.CONFLICT),
    
	//엑셀 다운로드 에러
	EXCEL_DOWNLOAD_NO_DATA("EXCEL_DOWNLOAD_001", "다운로드할 데이터가 없습니다.", HttpStatus.BAD_REQUEST);

    //----- Fields-----//
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    @Override
    public String toString() {
        return String.format("%s: %s", this.code, this.message);
    }
}