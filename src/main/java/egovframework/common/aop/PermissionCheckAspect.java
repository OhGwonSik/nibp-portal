package egovframework.common.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.admin.menu.domain.MenuDto;
import egovframework.admin.menu.mapper.MenuMapper;
import egovframework.admin.usermenuauth.domain.UserMenuAuthDto;
import egovframework.admin.usermenuauth.mapper.UserMenuAuthMapper;
import egovframework.common.annotation.CheckMenuAccess;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.CheckPortalBoardAccess;
import egovframework.common.annotation.LogParam;
import egovframework.common.audit.domain.PersonalInfoProcLog;
import egovframework.common.audit.service.AuditService;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.enums.PermissionType;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.exception.UnAuthenticatedException;
import egovframework.common.util.CryptoUtil;
import egovframework.common.util.RequestUtil;
import egovframework.portal.menu.domain.UserMenuDTO;
import egovframework.portal.menu.service.UserMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @ClassName : PermissionCheckAspect.java
 * @Description : 권한 체크 Aspect
 *
 * @author : tspark
 * @since  : 2025. 11. 13
 * @version : 1.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionCheckAspect {
    private final MenuMapper menuMapper;
    private final UserMenuAuthMapper userAuthMapper;
    private final HttpServletRequest request;
	private final UserMenuService userMenuService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final CryptoUtil cryptoUtil;

    /**
     * 로깅에서 제외할 민감 필드명 목록
     */
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
        "password", "pswd", "currentPswd", "newPswd", "confirmPswd", "oldPswd",
        "ssn", "pin", "securityCode", "cardNumber", "cvv",
        "token", "accessToken", "refreshToken", "secretKey"
    );

    // 메뉴 접근 체크 (메뉴 매핑 확인)
    @Before("@annotation(checkMenuAccess)")
    public void checkMenuAccess(JoinPoint joinPoint, CheckMenuAccess checkMenuAccess) {
        // 1. Annotation에서 menuPage 추출
        String menuPage = checkMenuAccess.menuPage();

        // menuPage가 비어있으면 JoinPoint의 PathVariable에서 추출
        if (menuPage == null || menuPage.isEmpty()) {
            menuPage = extractMenuPageFromPathVariable(joinPoint);
        }

        if (menuPage == null) {
            log.warn("메뉴 접근 체크 실패: menuPage를 추출할 수 없습니다.");
            throw new AccessDeniedException("잘못된 접근입니다.");
        }

        String removeSuffixMenuPage = menuPage.trim().split("-")[0]; // suffix 제거(ex admin100-1)

        // 2. menuPage를 menuOid 목록으로 변환
        List<MenuDto> menus = menuMapper.selectMenusByMenuPage(removeSuffixMenuPage);
        if (menus == null || menus.isEmpty()) {
            log.warn("메뉴 접근 체크 실패: 유효하지 않은 메뉴 페이지 ({})", removeSuffixMenuPage);
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        // 3. 메뉴의 menuAuthLv 확인 (첫 번째 메뉴 기준)
        MenuDto menu = menus.get(0);
        String menuAuthLv = menu.getMenuAuthLv();

        // 4. menuAuthLv에 따른 접근 제어
        if ("COMMON".equals(menuAuthLv)) {
            // COMMON: 인증 체크 없이 통과
            log.debug("메뉴 접근 체크 통과: 메뉴페이지({}), 권한레벨(COMMON)", removeSuffixMenuPage);
            return;
        }

        // 5. 인증 상태 확인 (USER, ADMIN 등)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication != null ? authentication.getPrincipal() : null;

        if (authentication == null || principal == null || !(principal instanceof BaseUser)) {
            log.warn("메뉴 접근 체크 실패: 인증되지 않은 사용자");
            throw new UnAuthenticatedException("로그인이 필요합니다.");
        }

        if ("USER".equals(menuAuthLv)) {
            // USER: 인증 상태만 확인하고 통과 (관리자 포함)
            log.debug("메뉴 접근 체크 통과: 메뉴페이지({}), 권한레벨(USER)", removeSuffixMenuPage);
            return;
        }

        // 6. 그 외의 경우 (ADMIN 등): 권한 매핑 체크
        Long userOid = ((BaseUser) principal).getUserOid();

        // 7. 사용자가 목록의 메뉴 중 하나라도 매핑되어 있는지 확인
        boolean hasAccess = menus.stream()
            .anyMatch(m -> {
                UserMenuAuthDto authData = userAuthMapper.selectValidAuth(userOid, m.getMenuOid(), LocalDate.now());
                return authData != null;
            });

        if (!hasAccess) {
            log.warn("메뉴 접근 체크 실패: 사용자({}), 메뉴페이지({}), 권한레벨({})에 대한 매핑 데이터 없음", userOid, removeSuffixMenuPage, menuAuthLv);
            throw new AccessDeniedException("해당 메뉴에 대한 접근 권한이 없습니다.");
        }

        log.debug("메뉴 접근 체크 통과: 사용자({}), 메뉴페이지({}), 권한레벨({})", userOid, removeSuffixMenuPage, menuAuthLv);
    }

    // API 체크
    // admin 고정 페이지 전용
    @Around("@annotation(checkMenuPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, CheckMenuPermission checkMenuPermission) throws Throwable {

        // 1. 인증 상태 확인
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null || !(principal instanceof BaseUser)) {
            throw new UnAuthenticatedException("로그인이 필요합니다.");
        }

        // 2. URL에서 menuPage 추출
        String menuPage = extractMenuPageFromUri(request.getRequestURI());

        if (menuPage == null) {
            log.warn("권한 체크 실패: URL에서 메뉴 페이지를 추출할 수 없습니다. (URI: {})", request.getRequestURI());
            throw new AccessDeniedException("잘못된 접근입니다.");
        }

        String removeSuffixMenuPage = menuPage.trim().split("-")[0]; // suffix 제거(ex admin100-1)

        // 3. 현재 로그인한 사용자 정보
        BaseUser userInfo = ((BaseUser) principal);
        Long userOid = userInfo.getUserOid();

        // 4. 어노테이션에서 필요한 권한 정보
        PermissionType requiredPermission = checkMenuPermission.permission();

        // 5. menuPage를 menuOid 목록으로 변환
        List<MenuDto> menus = menuMapper.selectMenusByMenuPage(removeSuffixMenuPage);
        if (menus == null || menus.isEmpty()) {
            log.warn("권한 체크 실패: 유효하지 않은 메뉴 페이지 ({})", removeSuffixMenuPage);
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        // 6. DB 권한 체크 (첫 번째 유효한 메뉴를 찾음)
        UserMenuAuthDto authData = null;
        MenuDto finalMenu = null;
        for (MenuDto menu : menus) {
            UserMenuAuthDto currentAuth = userAuthMapper.selectValidAuth(userOid, menu.getMenuOid(), LocalDate.now());
            if (currentAuth != null) {
                authData = currentAuth; // 유효한 권한 데이터를 찾음
                finalMenu = menu;
                break; // 첫 번째 유효한 메뉴를 찾았으므로 반복 중단
            }
        }

        if (authData == null || finalMenu == null) {
            // 이 페이지에 연결된 어떤 메뉴에도 사용자의 유효한 권한 정보가 없음
            log.warn("권한 체크 실패: 사용자({}), 메뉴페이지({}), 필요권한({}) 없음", userOid, removeSuffixMenuPage, requiredPermission);
            throw new AccessDeniedException("해당 기능에 대한 접근 권한이 없습니다.");
        }

        boolean hasPermission = false;
        String taskDetail = "";
        switch (requiredPermission) {
            case READ: {
                hasPermission = "Y".equals(authData.getInqAuthrtYn());
                taskDetail = requiredPermission.getName();
                break;
            }
            case WRITE: {
                hasPermission = "Y".equals(authData.getWrtAuthrtYn());
                taskDetail = requiredPermission.getName();
                break;
            }
            case DELETE: {
                hasPermission = "Y".equals(authData.getDelAuthrtYn());
                taskDetail = requiredPermission.getName();
                break;
            }
            case EXCEL: {
                hasPermission = "Y".equals(authData.getExcelAuthrtYn());
                taskDetail = requiredPermission.getName();
                break;
            }
            case PRINT: {
                hasPermission = "Y".equals(authData.getOtptAuthrtYn());
                taskDetail = requiredPermission.getName();
                break;
            }
        }

        if (!hasPermission) {
            log.warn("권한 체크 실패: 사용자({}), 메뉴({}), 필요권한({}) 없음", userOid, finalMenu.getMenuOid(), requiredPermission);
            throw new AccessDeniedException("해당 기능에 대한 접근 권한이 없습니다.");
        }

        // 7. 메서드 실행
        Object result = joinPoint.proceed();

        // 8. 개인정보 처리 로그 기록
        try {
            // 무조건 기록
            if("Y".equals(finalMenu.getPrvcUseYn()) || (requiredPermission == PermissionType.EXCEL && hasPermission)) {
                // @LogParam이 붙은 파라미터에서 dataSubject, reason 추출
                String dataSubject =  extractDataSubject(joinPoint);
                String reason = extractReason(joinPoint, taskDetail); // reason 추출 로직

                PersonalInfoProcLog personalInfoProcLog = PersonalInfoProcLog.builder()
                                                                        .acsuserOid(userOid)
                                                                        .acsId(userInfo.getUserId())
                                                                        .acsIp(RequestUtil.getRemoteIpAddress())
                                                                        .menuNm(finalMenu.getMenuNm())
                                                                        .flfmtTaskDtl(taskDetail)
                                                                        .infoPrcsSubj(dataSubject)
                                                                        .rsn(reason)
                                                                        .regId(userInfo.getUserId())
                                                                        .regDt(LocalDateTime.now())
                                                                        .build();

                auditService.insertPersonalInfoProcLog(cryptoUtil.encrypt(personalInfoProcLog));
            }
        } catch (Exception e) {
            // 로그 기록 실패해도 원래 API 응답은 정상 반환
            log.error("개인정보 처리 로그 기록 실패 (사용자: {}, 메뉴: {}): {}", userInfo.getUserId(), finalMenu.getMenuNm(), e.getMessage(), e);
        }

        return result;
    }

    /**
     * @LogParam이 붙은 파라미터를 JSON으로 직렬화하여 dataSubject로 반환
     * - DTO/Map: JSON으로 직렬화 (민감 필드 제외)
     * - 단일 값(String, Number 등): "파라미터명=값" 형태로 반환
     */
    private String extractDataSubject(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < args.length; i++) {
                for (Annotation annotation : parameterAnnotations[i]) {
                    if (annotation instanceof LogParam) {
                        Object param = args[i];
                        if (param != null) {
                            // 단일 값인 경우 (String, Number, Boolean 등)
                            if (isSimpleType(param)) {
                                String paramName = (parameterNames != null && parameterNames.length > i)
                                    ? parameterNames[i]
                                    : "value";
                                return paramName + ":" + param.toString();
                            }
                            // DTO, Map 등 복합 객체인 경우 - 민감 필드 제외 후 직렬화
                            Map<String, Object> filteredMap = filterSensitiveFields(param);
                            return objectMapper.writeValueAsString(filteredMap);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("dataSubject 추출 실패: {}", e.getMessage());
        }
        return "";
    }

    /**
     * 단일 값 타입인지 확인 (String, Number, Boolean 등)
     */
    private boolean isSimpleType(Object obj) {
        return obj instanceof String
            || obj instanceof Number
            || obj instanceof Boolean
            || obj instanceof Character
            || obj.getClass().isPrimitive();
    }

    /**
     * 객체에서 민감 필드를 제외한 Map 반환
     * - Map인 경우: 민감 필드 제거
     * - DTO인 경우: Map으로 변환 후 민감 필드 제거
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> filterSensitiveFields(Object obj) {
        Map<String, Object> map;

        if (obj instanceof Map) {
            map = new HashMap<>((Map<String, Object>) obj);
        } else {
            // DTO를 Map으로 변환
            map = objectMapper.convertValue(obj, Map.class);
        }

        // 민감 필드 마스킹 처리
        SENSITIVE_FIELDS.forEach(key -> {
            if (map.containsKey(key)) {
                map.put(key, "******");
            }
        });

        return map;
    }

    /**
     * @LogParam이 붙은 파라미터에서 reason 필드 추출
     * reason 필드가 없으면 기본값(taskDetail) 반환
     */
    private String extractReason(ProceedingJoinPoint joinPoint, String defaultReason) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < args.length; i++) {
                for (Annotation annotation : parameterAnnotations[i]) {
                    if (annotation instanceof LogParam) {
                        Object param = args[i];
                        if (param != null) {
                            String reason = extractReasonFromObject(param);
                            if (reason != null && !reason.trim().isEmpty()) {
                                return reason;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("reason 추출 실패: {}", e.getMessage());
        }
        return defaultReason;
    }

    /**
     * 객체에서 reason 필드 추출 (Map, DTO 모두 지원)
     */
    private String extractReasonFromObject(Object obj) {
        try {
            // Map인 경우
            if (obj instanceof Map) {
                Object reason = ((Map<?, ?>) obj).get("reason");
                if (reason instanceof String) {
                    return (String) reason;
                }
            }
            // DTO인 경우 - 리플렉션으로 getReason() 메서드 호출
            else {
                Method getReason = obj.getClass().getMethod("getReason");
                Object result = getReason.invoke(obj);
                if (result instanceof String) {
                    return (String) result;
                }
            }
        } catch (NoSuchMethodException e) {
            // reason 필드/메서드가 없는 경우 무시
        } catch (Exception e) {
            log.warn("reason 필드 추출 중 오류: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 포털 동적 게시판 접근 권한(menu_auth_lv) 체크
     */
    @Before("@annotation(checkPortalBoardAccess)")
    public void checkPortalBoardAccess(JoinPoint joinPoint, CheckPortalBoardAccess checkPortalBoardAccess) {
        UserMenuDTO menu = null;
        String menuCodeFromAnnotation = checkPortalBoardAccess.menuCd();

        // 1. 어떤 메뉴인지 식별
        if (menuCodeFromAnnotation != null && !menuCodeFromAnnotation.isEmpty()) {
            // Case 1: 어노테이션에 menuCode가 지정된 경우 (고정 게시판)
            menu = userMenuService.selectMenuByMenuCd(menuCodeFromAnnotation);
        } else {
            // 2. 어노테이션에 menuCode가 없는 경우 (동적 게시판)
    		String dynamicMenuCd = getMenuCdDynamically(joinPoint);
            Long dynamicBbsOid = getBbsOidDynamically(joinPoint);

            if (dynamicMenuCd != null && dynamicBbsOid != null) {
                // menuCd와 bbsOid가 모두 있으면 메뉴-게시판 연결까지 검증
                menu = userMenuService.selectValidBoardMenu(dynamicMenuCd, dynamicBbsOid);
            } else if (dynamicMenuCd != null) {
                menu = userMenuService.selectMenuByMenuCd(dynamicMenuCd);
            }
        }

        if (menu == null || menu.getMenuAuthLv() == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시판 메뉴 정보가 없거나 접근 정보가 설정되지 않았습니다.");
        }

        String menuAuthLv = menu.getMenuAuthLv();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        switch (menuAuthLv) {
            case "USER":
                if (authentication == null || !authentication.isAuthenticated() || (authentication instanceof AnonymousAuthenticationToken)) {
                    throw new UnAuthenticatedException("로그인이 필요한 게시판입니다.");
                }
                // USER: 인증 상태만 확인하고 통과 (관리자 포함)
                break;
            case "ADMIN":
                if (authentication == null || authentication.getAuthorities().stream()
                        .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                    throw new AccessDeniedException("관리자만 접근 가능한 게시판입니다.");
                }
                break;
            case "COMMON":
            default:
                break;
        }

        log.debug("포털 게시판 접근 체크 통과: 메뉴({}), 필요 권한({})", menu.getMenuCd(), menuAuthLv);
    }

    /**
     * URI에서 menuPage를 추출하는 메서드 (API용)
     */
    private String extractMenuPageFromUri(String uri) {
        try {
            String[] parts = uri.split("/");
            if (parts.length > 3 && "api".equals(parts[1]) && "admin".equals(parts[2])) {
                return parts[3]; // ex) "admin802"
            }
            return null;
        } catch (Exception e) {
            log.error("menuCode 추출 중 오류 => {}", e.getMessage());
            return null;
        }
    }

    /**
     * JoinPoint의 PathVariable에서 menuPage를 추출하는 메서드 (페이지 접근용)
     */
    private String extractMenuPageFromPathVariable(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            // targetPage는 보통 첫 번째 파라미터 (@PathVariable("targetPage") String targetPage)
            if (args.length > 0 && args[0] instanceof String) {
                return (String) args[0];
            }
            return null;
        } catch (Exception e) {
            log.error("PathVariable에서 menuPage 추출 중 오류 => {}", e.getMessage());
            return null;
        }
    }

    // JoinPoint에서 특정 타입의 인자를 찾는 헬퍼 메서드 (필요시 추가)
    private <T> T findArgument(JoinPoint joinPoint, Class<T> clazz) {
        for (Object arg : joinPoint.getArgs()) {
            if (clazz.isInstance(arg)) {
                return (T) arg;
            }
        }
        return null;
    }

    /**
     * 파라미터 목록에서 'bbsOid' 값을 동적으로 추출하는 메서드
     * 지원 타입:
     * 1. @RequestParam Long bbsOid
     * 2. DTO/VO 객체 (getBbsOid() 메서드 보유)
     */
    private Long getBbsOidDynamically(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) continue;

            // 1. 파라미터 이름이 "bbsOid"인 경우
            if (paramNames != null && "bbsOid".equals(paramNames[i])) {
                if (arg instanceof Long) {
                    return (Long) arg;
                }
                if (arg instanceof Number) {
                    return ((Number) arg).longValue();
                }
                if (arg instanceof String) {
                    try { return Long.parseLong((String) arg); } catch (NumberFormatException ignored) {}
                }
            }

            // 2. DTO/VO 리플렉션 (getBbsOid())
            if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
                continue;
            }

            Object targetObject = arg;
            if (arg instanceof Collection) {
                Collection<?> collection = (Collection<?>) arg;
                if (collection.isEmpty()) continue;
                targetObject = collection.iterator().next();
            }

            try {
                Method method = targetObject.getClass().getMethod("getBbsOid");
                Object result = method.invoke(targetObject);
                if (result instanceof Long) {
                    return (Long) result;
                }
                if (result instanceof Number) {
                    return ((Number) result).longValue();
                }
            } catch (NoSuchMethodException e) {
                continue;
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    /**
     * 파라미터 목록에서 'menuCd' 값을 동적으로 추출하는 메서드
     * 지원 타입:
     * 1. @RequestParam String menuCd
     * 2. DTO/VO 객체 (getMenuCd() 메서드 보유)
     * 3. List<DTO>, List<VO> (첫 번째 요소의 getMenuCd() 실행)
     */
    private String getMenuCdDynamically(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames(); // 파라미터 이름들
        Object[] args = joinPoint.getArgs();                 // 파라미터 값들

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) continue;

            // 1. 단순 String 변수인데 이름이 "menuCd" 인 경우
            if (paramNames != null && "menuCd".equals(paramNames[i])) {
                if (arg instanceof String && !((String) arg).isEmpty()) {
                    return (String) arg;
                }
            }
            // 2. 리플렉션 대상 객체 결정 (단일 객체 vs 리스트)
            Object targetObject = arg;

            // 만약 List(Collection)라면 첫 번째 요소를 꺼내서 검사 대상으로 삼음
            if (arg instanceof Collection) {
                Collection<?> collection = (Collection<?>) arg;
                if (collection.isEmpty()) continue; // 빈 리스트는 패스
                targetObject = collection.iterator().next(); // 첫 번째 객체 추출
            }

            // 3. getMenuCd() 메서드 실행 (Reflection)
            try {
                // 기본 타입(String, Integer 등)은 getMenuCd가 없으므로 패스 (성능 최적화)
                if (targetObject instanceof String || targetObject instanceof Number || targetObject instanceof Boolean) {
                    continue;
                }

                Method method = targetObject.getClass().getMethod("getMenuCd");
                Object result = method.invoke(targetObject);

                if (result instanceof String && !((String) result).isEmpty()) {
                    return (String) result;
                }
            } catch (NoSuchMethodException e) {
                continue;
            } catch (Exception e) {
            }
        }
        
        return null; // 모든 파라미터를 다 뒤져도 못 찾음
    }
}
