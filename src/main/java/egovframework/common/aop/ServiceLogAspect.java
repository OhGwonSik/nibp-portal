package egovframework.common.aop;

import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName : ServiceLogAspect.java
 * @Description : 서비스 레이어 로그 Aspect
 *
 * @author : t.s.park
 * @since  : 2026. 02. 06
 * @version : 1.0
 */
@Aspect
@Slf4j
@Component
public class ServiceLogAspect {

    private static final Set<String> SENSITIVE_PARAM_KEYWORDS = Set.of(
            "password", "pswd", "secret", "credential", "token",
            "pwd", "pass", "pw", "wrtrPswd"
    );

    @Around("execution(* egovframework..service.impl..*.*(..))")
    public Object serviceLogExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        // 실행된 클래스와 메소드 이름
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        String methodName = joinPoint.getSignature().getName();

        // 파라미터 이름 조회 (민감정보 마스킹용)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();

        // 파라미터 정보 (민감정보 마스킹)
        String params = maskSensitiveParams(joinPoint.getArgs(), paramNames);

        // [시작 로그]
        log.info(">>>> [SERVICE START] {}.{}() | Params: {}", simpleClassName, methodName, params);

        long start = System.currentTimeMillis();

        try {
            // 실제 비즈니스 로직(서비스 메소드) 실행
            Object result = joinPoint.proceed();

            // [종료 로그] 실행 시간 계산
            long executionTime = System.currentTimeMillis() - start;

            // 결과 로그 (너무 긴 경우 축약)
            String resultStr = summarizeResult(result);
            log.info("<<<< [SERVICE END]   {}.{}() | Time: {}ms | Result: {}", simpleClassName, methodName, executionTime, resultStr);

            // 느린 쿼리/서비스 경고 (500ms 이상)
            if (executionTime > 500) {
                log.warn("[SLOW SERVICE] {}.{}() took {}ms", simpleClassName, methodName, executionTime);
            }

            return result;
        } catch (Exception e) {
            // [에러 로그] 예외 발생 시
            long executionTime = System.currentTimeMillis() - start;
            log.error("!!!! [SERVICE ERROR] {}.{}() | Time: {}ms | Exception: {} - {}",
                    simpleClassName, methodName, executionTime, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    /**
     * 민감한 파라미터 마스킹 처리
     * - 파라미터 이름에 민감 키워드(password, pswd, secret, credential, token)가 포함되면 마스킹
     * - 클래스 이름에 민감 키워드가 포함된 DTO도 마스킹
     */
    private String maskSensitiveParams(Object[] args, String[] paramNames) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
                continue;
            }

            String argClassName = arg.getClass().getSimpleName();

            // 파라미터 이름 기반 마스킹 (예: String password, String token)
            if (paramNames != null && i < paramNames.length && isSensitiveName(paramNames[i])) {
                sb.append(paramNames[i]).append(":[MASKED]");
            }
            // 클래스 이름 기반 마스킹 (예: PasswordResetConfirmDto, AdminUserUpdatePwdDto)
            else if (isSensitiveName(argClassName)) {
                sb.append(argClassName).append(":[MASKED]");
            }
            // 너무 긴 파라미터 축약
            else {
                String argStr = arg.toString();
                if (argStr.length() > 100) {
                    sb.append(argClassName).append(":[").append(argStr, 0, 100).append("...]");
                } else {
                    sb.append(argStr);
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private boolean isSensitiveName(String name) {
        String lower = name.toLowerCase();
        return SENSITIVE_PARAM_KEYWORDS.stream().anyMatch(lower::contains);
    }

    /**
     * 결과 요약 (너무 긴 경우 축약)
     */
    private String summarizeResult(Object result) {
        if (result == null) {
            return "null";
        }

        String resultStr = result.toString();
        if (resultStr.length() > 200) {
            return result.getClass().getSimpleName() + ":[" + resultStr.substring(0, 200) + "...]";
        }
        return resultStr;
    }
}
