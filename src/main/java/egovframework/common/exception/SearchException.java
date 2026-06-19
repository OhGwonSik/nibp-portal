package egovframework.common.exception;

/**
 * @ClassName : SearchException.java
 * @Description : 검색 예외
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
public class SearchException extends BusinessException {
    public SearchException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public SearchException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }
    
    public SearchException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public SearchException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }
}
