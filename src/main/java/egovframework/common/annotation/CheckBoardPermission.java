package egovframework.common.annotation;

import egovframework.common.enums.AuthLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @ClassName : CheckBoardPermission.java
 * @Description : 게시판 API 접근 권한 체크 annotation
 *
 * @author : tspark
 * @since  : 2025. 11. 25
 * @version : 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckBoardPermission {
    AuthLevel menuAuthLv();
}
