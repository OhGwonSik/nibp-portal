package egovframework.common.annotation;

import egovframework.common.enums.MaskingType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 마스킹 처리가 필요한 필드에 사용하는 어노테이션
 * MaskingUtil을 통해 자동으로 마스킹됩니다.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Masked {
    /**
     * 마스킹 타입
     */
    MaskingType type();
}
