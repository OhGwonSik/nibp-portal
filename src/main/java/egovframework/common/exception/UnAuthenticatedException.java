package egovframework.common.exception;

/**
 * @ClassName : UnAuthenticatedException.java
 * @Description : 미인증 예외
 *
 * @author : tspark
 * @since  : 2025. 11. 13
 * @version : 1.0
 */
public class UnAuthenticatedException extends RuntimeException {

    public UnAuthenticatedException(String message) {
        super(message);
    }

    public UnAuthenticatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
