package egovframework.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName : ErrorResponse.java
 * @Description : API 에러 응답 DTO
 *
 * @author : tspark
 * @since  : 2025. 11. 05
 * @version : 1.0
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일              수정자               수정내용
 *  -------------  ------------   ---------------------
 *   2025. 11. 05    tspark          최초 생성
 * </pre>
 *
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String code;
    private final String message;
    private final String details;

    @Builder.Default
    private final List<ValidationError> validationErrors = new ArrayList<>();

    /**
     * 유효성 검증 에러 상세 정보
     */
    @Getter
    @Builder
    public static class ValidationError {
        private final String field;
        private final String rejectedValue;
        private final String message;
    }

    /**
     * ErrorCode로부터 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    /**
     * ErrorCode와 상세 정보로부터 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String details) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(details)
                .build();
    }

    /**
     * HTTP 상태 코드로부터 ErrorResponse 생성
     */
    public static ErrorResponse of(int status, String error, String message) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .build();
    }
}
