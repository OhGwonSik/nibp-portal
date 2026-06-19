package egovframework.common.annotation;

import egovframework.common.enums.PermissionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @ClassName : CheckMenuPermission.java
 * @Description : 메뉴 API 접근 권한 체크 annotation
 *
 * @author : tspark
 * @since  : 2025. 11. 13
 * @version : 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckMenuPermission {
    PermissionType permission();
}