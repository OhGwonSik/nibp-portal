package egovframework.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 개인정보 처리 로그에 기록할 파라미터를 지정하는 어노테이션
 *
 * <p>해당 어노테이션이 붙은 파라미터는 JSON으로 직렬화되어
 * PersonalInfoProcLog.dataSubject에 암호화 저장.</p>
 *
 * <p>파라미터에 'reason' 필드가 포함되어 있으면 자동으로 추출하여
 * PersonalInfoProcLog.reason에 저장. (엑셀 다운로드 등)</p>
 *
 * @author tspark
 * @since 2025. 01. 16
 * @version 1.0
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogParam {
}
