package egovframework.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 포털 동적 게시판의 접근 권한(menu_auth_lv)을 체크하는 어노테이션.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckPortalBoardAccess {
    String menuCd() default ""; 
}
