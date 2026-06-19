package egovframework.common.exception;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import egovframework.common.util.RequestTypeUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName : GlobalExceptionHandler.java
 * @Description : 전역 예외 핸들러
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
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     * API 요청: JSON 응답
     * 페이지 요청: 에러 페이지로 이동
     */
    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        log.error("비즈니스 예외 발생: 코드={}, 메시지={}, 상세={}, 경로={}, 메소드={}",
                ex.getErrorCode().getCode(),
                ex.getMessage(),
        ex.getDetails(),
                request.getRequestURI(),
                request.getMethod(),
                ex);

        // API 요청인 경우 JSON 응답
        if (RequestTypeUtil.isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    ex.getErrorCode(),
                    ex.getDetails());
            return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
        }

        // 페이지 요청인 경우 에러 페이지로 이동
        ModelAndView mav = new ModelAndView("common/error");
        // mav.addObject("errorMessage", ex.getMessage());
        // mav.addObject("errorCode", ex.getErrorCode().getCode());
        // mav.addObject("errorDetails", ex.getDetails());
        // mav.setStatus(ex.getErrorCode().getHttpStatus());
        return mav;
    }

    /**
     * 유효성 검증 실패 - @Valid 어노테이션 사용 시
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("유효성 검증 실패: 경로={}, 메소드={}, 필드 수={}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getBindingResult().getFieldErrors().size(),
                ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .code(ErrorCode.VALIDATION_FAILED.getCode())
                .message(ErrorCode.VALIDATION_FAILED.getMessage())
                .validationErrors(
                        ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> ErrorResponse.ValidationError.builder()
                                        .field(error.getField())
                                        .message(error.getDefaultMessage())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 유효성 검증 실패 - 폼 데이터 바인딩 시
     * API 요청: JSON 응답
     * 페이지 요청: 에러 페이지로 이동
     */
    @ExceptionHandler(BindException.class)
    public Object handleBindException(
            BindException ex,
            HttpServletRequest request) {

        log.warn("바인딩 유효성 검증 실패: 경로={}, 메소드={}, 필드 수={}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getBindingResult().getFieldErrors().size(),
                ex);

        // API 요청인 경우 JSON 응답
        if (RequestTypeUtil.isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(java.time.LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .code(ErrorCode.VALIDATION_FAILED.getCode())
                    .message(ErrorCode.VALIDATION_FAILED.getMessage())
                    .validationErrors(
                            ex.getBindingResult().getFieldErrors().stream()
                                    .map(error -> ErrorResponse.ValidationError.builder()
                                            .field(error.getField())
                                            .rejectedValue((error !=null && error.getRejectedValue() != null) ?
                                                    error.getRejectedValue().toString() : null)
                                            .message(error.getDefaultMessage())
                                            .build())
                                    .collect(Collectors.toList())
                    )
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // 페이지 요청인 경우 에러 페이지로 이동
        return new ModelAndView("common/error");
    }

    /**
     * 필수 요청 파라미터 누락
     * API 요청: JSON 응답
     * 페이지 요청: 에러 페이지로 이동
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        log.warn("필수 요청 파라미터 누락: {}, 경로={}, 메소드={}",
                ex.getParameterName(),
                request.getRequestURI(),
                request.getMethod(),
                ex);

        // API 요청인 경우 JSON 응답
        if (RequestTypeUtil.isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    ErrorCode.REQUIRED_FIELD_MISSING,
                    "필수 파라미터 '" + ex.getParameterName() + "'가 누락되었습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // 페이지 요청인 경우 에러 페이지로 이동
        return new ModelAndView("common/error");
    }

    /**
     * 잘못된 요청 데이터 형식
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("잘못된 요청 형식: 경로={}, 메소드={}",
                request.getRequestURI(),
                request.getMethod(),
                ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_REQUEST_FORMAT,
                "요청 본문이 유효하지 않거나 잘못된 형식입니다.");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 메소드 인자 타입 불일치
     * API 요청: JSON 응답
     * 페이지 요청: 에러 페이지로 이동
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Object handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.warn("메소드 인자 타입 불일치: 파라미터={}, 타입={}, 경로={}, 메소드={}",
                ex.getName(),
                ex.getRequiredType(),
                request.getRequestURI(),
                request.getMethod(),
                ex);

        // API 요청인 경우 JSON 응답
        if (RequestTypeUtil.isApiRequest(request)) {
            String details = String.format("파라미터 '%s'의 타입은 '%s'이어야 합니다.",
                    ex.getName(),
                    ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "알 수 없음");

            ErrorResponse errorResponse = ErrorResponse.of(
                    ErrorCode.VALIDATION_FAILED,
                    details);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // 페이지 요청인 경우 에러 페이지로 이동
        return new ModelAndView("common/error");
    }

    /**
     * 지원하지 않는 HTTP 메소드
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        log.warn("지원하지 않는 HTTP 메소드: 메소드={}, 경로={}",
                ex.getMethod(),
                request.getRequestURI(),
                ex);

        String details = String.format("이 엔드포인트는 '%s' HTTP 메소드를 지원하지 않습니다.", ex.getMethod());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.OPERATION_NOT_ALLOWED,
                details);

        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 404 Not Found - 핸들러를 찾을 수 없음
     * API 요청: JSON 응답
     * 페이지 요청: 에러 페이지로 이동
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpServletRequest request) {

        log.warn("핸들러를 찾을 수 없음: 메소드={}, 경로={}",
                ex.getHttpMethod(),
                ex.getRequestURL(),
                ex);

        // API 요청인 경우 JSON 응답
        if (RequestTypeUtil.isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "요청하신 리소스를 찾을 수 없습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        // 페이지 요청인 경우 에러 페이지로 이동
        return new ModelAndView("common/error");
    }

    /**
     * Spring Security - 인증 실패 통합 처리
     * API 요청: JSON 응답 (401)
     * 페이지 요청: 로그인 페이지로 리다이렉트
     *
     * 처리 대상:
     * - BadCredentialsException: 비밀번호 오류
     * - LockedException: 계정 잠김
     * - DisabledException: 계정 비활성화
     */
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class, LockedException.class, DisabledException.class})
    public Object handleAuthenticationExceptions(
            Exception ex,
            HttpServletRequest request) {

        // 예외 타입별 로깅 및 에러코드 결정
        ErrorCode errorCode;

        if (ex instanceof UsernameNotFoundException || ex instanceof BadCredentialsException) {
            errorCode = ErrorCode.AUTHENTICATION_FAILED;
            log.warn("인증 실패 - 잘못된 자격 증명: 경로={}, 메소드={}",
                    request.getRequestURI(),
                    request.getMethod(),
                    ex);
        } else if (ex instanceof LockedException) {
            errorCode = ErrorCode.ACCOUNT_LOCKED;
            log.warn("인증 실패 - 계정 잠김: 경로={}, 메소드={}",
                    request.getRequestURI(),
                    request.getMethod(),
                    ex);
        } else if (ex instanceof DisabledException) {
            errorCode = ErrorCode.ACCOUNT_DISABLED;
            log.warn("인증 실패 - 비활성화된 계정: 경로={}, 메소드={}",
                    request.getRequestURI(),
                    request.getMethod(),
                    ex);
        } else {
            errorCode = ErrorCode.AUTHENTICATION_FAILED;
            log.warn("인증 실패 - 알 수 없는 오류: 경로={}, 메소드={}",
                    request.getRequestURI(),
                    request.getMethod(),
                    ex);
        }

        // API 요청인 경우 JSON 응답
        if (RequestTypeUtil.isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(errorCode);
            return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
        }

        // 페이지 요청인 경우 로그인 페이지로 리다이렉트
        try {
            String redirectUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/login")
                    .toUriString();
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            log.error("인증 실패 리다이렉트 처리 중 오류 발생", e);
            return "redirect:/login?error=인증_실패";
        }
    }

    /**
     * 미인증 사용자 접근
     * API 요청: JSON 응답 (401)
     * 페이지 요청: 로그인 페이지로 리다이렉트
     */
    @ExceptionHandler(UnAuthenticatedException.class)
    public Object handleUnAuthenticatedException(
            UnAuthenticatedException ex,
            HttpServletRequest request) {

        log.warn("인증되지 않은 접근: 경로={}, 메소드={}",
                request.getRequestURI(),
                request.getMethod(),
                ex);

        // API 요청인 경우 JSON 응답
        if (RequestTypeUtil.isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.AUTHENTICATION_FAILED);
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // 페이지 요청인 경우 로그인 페이지로 리다이렉트
        String path = RequestTypeUtil.getActualPath(request);

        // 관리자 페이지와 포탈 페이지의 로그인 페이지 구분
        if (RequestTypeUtil.isAdminPageRequest(path)) {
            return "redirect:" + ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/admin/login")
                    .toUriString();
        }

        if (RequestTypeUtil.isPortalPageRequest(path)) {
            return "redirect:" + ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/login")
                    .toUriString();
        }

        // 기본값: 일반 로그인 페이지
        return "redirect:" + ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/login")
                .toUriString();
    }

    /**
     * Spring Security - 접근 거부
     * API 요청: JSON 응답 (403)
     * 페이지 요청: 이전 페이지로 리다이렉트 (error 파라미터 포함)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("접근 거부됨: 경로={}, 메소드={}",
                request.getRequestURI(),
                request.getMethod(),
                ex);

        // API 요청인 경우 JSON 응답
        if (RequestTypeUtil.isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INSUFFICIENT_PERMISSIONS);
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        // 페이지 요청인 경우 리다이렉트 처리
        String contextPath = request.getContextPath();
        String path = RequestTypeUtil.getActualPath(request);

        // 관리자 페이지 요청 처리
        if (RequestTypeUtil.isAdminPageRequest(path)) {
            return handleAccessDeniedRedirect(request, contextPath + "/admin/main");
        }

        // 포탈 페이지 요청 처리
        if (RequestTypeUtil.isPortalPageRequest(path)) {
            return handleAccessDeniedRedirect(request, contextPath + "/main");
        }

        // 기본값: 이전 페이지로 리다이렉트
        return handleAccessDeniedRedirect(request, contextPath + "/main");
    }

    /**
     * 데이터베이스 예외
     * API 요청: JSON 응답
     * 페이지 요청: 에러 페이지로 이동
     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public Object handleDataAccessException(
            org.springframework.dao.DataAccessException ex,
            HttpServletRequest request) {

        log.error("데이터베이스 오류 발생: 경로={}, 메소드={}",
                request.getRequestURI(),
                request.getMethod(),
                ex);

        // API 요청인 경우 JSON 응답
        if (RequestTypeUtil.isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    ErrorCode.DATABASE_ERROR,
                    "데이터베이스 오류가 발생했습니다. 나중에 다시 시도해 주세요.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 페이지 요청인 경우 에러 페이지로 이동
        return new ModelAndView("common/error");
    }

    /**
     * 파일을 찾을 수 없는 경우
     * API 요청: JSON 응답
     * 페이지 요청: 에러 페이지로 이동
     */
    @ExceptionHandler(java.io.FileNotFoundException.class)
    public Object handleFileNotFoundException(
            java.io.FileNotFoundException ex,
            HttpServletRequest request) {

        log.error("파일을 찾을 수 없음: 경로={}, 메소드={}, 파일={}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getMessage(),
                ex);

        // API 요청인 경우 JSON 응답
        if (RequestTypeUtil.isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    ErrorCode.FILE_NOT_FOUND,
                    "요청하신 파일을 찾을 수 없습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        // 페이지 요청인 경우 에러 페이지로 이동
        return new ModelAndView("common/error");
    }

    /**
     * IOException - 파일 처리 등 입출력 오류
     * API 요청: JSON 응답
     * 페이지 요청: 에러 페이지로 이동
     */
    @ExceptionHandler(java.io.IOException.class)
    public Object handleIOException(
            java.io.IOException ex,
            HttpServletRequest request) {

        log.error("입출력 오류 발생: 경로={}, 메소드={}, 상세={}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getMessage(),
                ex);

        // API 요청인 경우 JSON 응답
        if (RequestTypeUtil.isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    ErrorCode.FILE_DOWNLOAD_FAILED,
                    "파일 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 페이지 요청인 경우 에러 페이지로 이동
        return new ModelAndView("common/error");
    }

    /**
     * IllegalArgumentException - 잘못된 인자값
     * API 요청: JSON 응답
     * 페이지 요청: 에러 페이지로 이동
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("잘못된 인자값: 경로={}, 메소드={}, 상세={}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getMessage(),
                ex);

        // API 요청인 경우 JSON 응답
        if (RequestTypeUtil.isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    ErrorCode.VALIDATION_FAILED,
                    ex.getMessage() != null ? ex.getMessage() : "잘못된 요청 파라미터입니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // 페이지 요청인 경우 에러 페이지로 이동
        return new ModelAndView("common/error");
    }

    /**
     * MessagingException - 이메일 발송 오류
     */
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ErrorResponse> handleMessagingException(
            MessagingException ex,
            HttpServletRequest request) {

        log.error("이메일 발송 오류 발생: 경로={}, 메소드={}, 상세={}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getMessage(),
                ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.EXTERNAL_SERVICE_ERROR,
                "이메일 발송 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 모든 예외를 처리하는 최종 핸들러
     * API 요청: JSON 응답
     * 페이지 요청: 에러 페이지로 이동
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(
            Exception ex,
            HttpServletRequest request) {

        log.error("예기치 않은 오류 발생: 경로={}, 메소드={}",
                request.getRequestURI(),
                request.getMethod(),
                ex);

        // API 요청인 경우 JSON 응답
        if (RequestTypeUtil.isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "예기치 않은 오류가 발생했습니다. 관리자에게 문의해 주세요.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 페이지 요청인 경우 에러 페이지로 이동
        return new ModelAndView("common/error");
    }

    /**
     * 접근 거부 시 리다이렉트 처리
     */
    private String handleAccessDeniedRedirect(HttpServletRequest request, String defaultRedirectUrl) {
        try {
            String referer = request.getHeader("Referer");
            String redirectUrl = (referer != null && !referer.isEmpty()) ? referer : defaultRedirectUrl;

            // 기존 error 파라미터 제거
            if (redirectUrl.contains("error=")) {
                redirectUrl = redirectUrl.replaceAll("([&?])error=[^&]*(&?)", "$1").replaceAll("[&?]$", "");
            }
            
            // 새로운 error 파라미터 추가
            redirectUrl += (redirectUrl.contains("?") ? "&" : "?") + "error=" + URLEncoder.encode("해당 메뉴에 접근 권한이 없습니다.", "UTF-8");

            return "redirect:" + redirectUrl;
        } catch (UnsupportedEncodingException e) {
            log.error("접근 거부 리다이렉트 처리 중 인코딩 오류 발생", e);
            return "redirect:" + defaultRedirectUrl + "?error=접근_거부됨";
        }    
    }
}
