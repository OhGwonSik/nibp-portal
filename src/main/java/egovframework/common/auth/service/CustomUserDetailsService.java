package egovframework.common.auth.service;

import egovframework.common.auth.domain.BaseUser;
import egovframework.common.auth.mapper.AuthMapper;
import egovframework.common.component.AESComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @ClassName : CustomUserDetailsService.java
 * @Description : 사용자 정보를 불러오는 UserDetailsService 구현체
 *
 * @author : 표준프레임워크센터
 * @since  : 2023. 07. 28
 * @version : 1.0
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일              수정자               수정내용
 *  -------------  ------------   ---------------------
 *   2025. 11. 04    tspark          DB 기반으로 변경
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final AuthMapper authMapper;
    private final AESComponent aesComponent;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user: {}", username);

        BaseUser user = null;
        String userId = username;

        try {
            user = authMapper.selectActiveUserByUserId(userId, aesComponent.getSecretKey());

            if (user != null) {
                log.debug("User found in database: {}", userId);
                return user;
            }

        } catch (Exception e) {
            log.error("Database query failed for user: {}", username, e);
            throw new AuthenticationServiceException("Database error while loading user: " + username, e);
        }

        log.warn("User not found: {}", username);
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}