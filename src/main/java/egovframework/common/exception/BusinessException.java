package egovframework.common.exception;
/**
 * @ClassName : BusinessException.java
 * @Description : 비즈니스 예외
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
public class BusinessException extends RuntimeException {
    //----- Fields-----//
    private final ErrorCode errorCode;
    private final String details;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public BusinessException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public BusinessException(ErrorCode errorCode, String customMessage, String details) {
        super(customMessage);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public BusinessException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public String getDetails() {
        return details;
    }
}