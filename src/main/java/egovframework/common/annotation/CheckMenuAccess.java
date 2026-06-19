package egovframework.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @ClassName : CheckMenuAccess.java
 * @Description : 메뉴 접근 권한 체크 annotation
 *
 * @author : tspark
 * @since  : 2025. 11. 13
 * @version : 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckMenuAccess {
    String menuPage() default "";
}
