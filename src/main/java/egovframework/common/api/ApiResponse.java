package egovframework.common.api;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @ClassName : ApiResponse.java
 * @Description : API 응답을 위한 표준 래퍼 클래스
 *
 * @param <T> 응답 데이터의 타입
 */
@Getter
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    private ApiResponse(HttpStatus status, String message, T data) {
        this.code = status.value();
        this.message = message;
        this.data = data;
    }

    /**
     * 성공 응답을 생성합니다. (데이터 포함)
     * @param data 응답 데이터
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatus.OK, "Success", data);
    }

    /**
     * 성공 응답을 생성합니다. (데이터 없음)
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(HttpStatus.OK, "Success", null);
    }

    /**
     * 에러 응답을 생성합니다.
     * @param status HTTP 상태 코드
     * @param message 에러 메시지
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return new ApiResponse<>(status, message, null);
    }

    /**
     * 커스텀 응답을 생성합니다. (상태 코드, 메시지, 데이터 지정)
     * @param code HTTP 상태 코드 값
     * @param message 메시지
     * @param data 응답 데이터
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> of(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
