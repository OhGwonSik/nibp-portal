package egovframework.common.aop;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;


/**
 * @ClassName : ControllerLogAspect.java
 * @Description : 컨트롤러 로그 Aspect
 *
 * @author : tspark
 * @since  : 2026. 01. 26
 * @version : 1.0
 */
@Aspect
@Slf4j
@Component
public class ControllerLogAspect {
    
    @Around("execution(* egovframework..controller..*.*(..))")
    public Object controllerLogExcution(ProceedingJoinPoint joinPoint) throws Throwable {// 1. 요청 정보 추출 (URL, QueryString, Method)
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        
        String url = request.getRequestURI();
        String method = request.getMethod();
        
        // 실행된 클래스와 메소드 이름
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        // [시작 로그]
        log.info(">>>> [START] {} {} | Method: {}.{}", method, url, className, methodName);

        long start = System.currentTimeMillis();
        
        try {
            // 실제 비즈니스 로직(컨트롤러 메소드) 실행
            Object result = joinPoint.proceed();

            // [종료 로그] 실행 시간 계산 및 결과 포함
            long executionTime = System.currentTimeMillis() - start;
            log.info("<<<< [END]   {} {} | Time: {}ms | Return: {}", method, url, executionTime, result);
            
            return result;
        } catch (Exception e) {
            // [에러 로그] 예외 발생 시
            log.error("!!!! [ERROR] {} {} | Message: {}", method, url, e.getMessage());
            throw e;
        }
    }
}   
