package egovframework.common.util;

import egovframework.common.auth.domain.BaseUser;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class SecurityUtil {
	public static SecurityContext getContext() {
		return SecurityContextHolder.getContext();
	}

	public static Authentication getAuthentication() {
		return getContext().getAuthentication();
	}

	public static Object getPrincipal() {
		return getAuthentication().getPrincipal();
	}

	public static BaseUser getUser() {
		if(getPrincipal() instanceof BaseUser user) {
			return user;
		}

		return null;
	}
}
