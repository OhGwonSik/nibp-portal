package egovframework.portal.menu.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import egovframework.common.board.domain.BoardRequestDto;
import egovframework.common.enums.AuthLevel;
import egovframework.portal.menu.domain.UserMenuDTO;
import egovframework.portal.menu.mapper.UserMenuMapper;
import egovframework.portal.menu.service.UserMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName : UserMenuServiceImpl.java
 * @Description : 사용자 메뉴 관리 서비스 구현체
 *
 * @author : balee
 * @since  : 2025. 12. 17
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserMenuServiceImpl extends EgovAbstractServiceImpl implements UserMenuService {
    private final UserMenuMapper userMenuMapper;

    private static final String MENU_LIST_CACHE_KEY = "menuList";
    private static final String ROOT_MENU_LIST_CACHE_KEY = "rootMenuList";

    @Value("${app.cache.menu.expire-seconds:30}")
    private int menuCacheExpireSeconds;

    private Cache<String, List<UserMenuDTO>> menuCache;

    @PostConstruct
    public void init() {
        menuCache = Caffeine.newBuilder()
            .expireAfterWrite(menuCacheExpireSeconds, TimeUnit.SECONDS)
            .build();
    }

    @Override
    public List<UserMenuDTO> selectMenuList() {
        List<UserMenuDTO> cachedList = menuCache.getIfPresent(MENU_LIST_CACHE_KEY);
        if (cachedList != null) {
            log.debug("Serving menu list from cache");
            return cachedList;
        }

        log.debug("Fetching menu list from database");
        List<UserMenuDTO> menuList = userMenuMapper.selectMenuList(AuthLevel.COMMON.name());
        menuCache.put(MENU_LIST_CACHE_KEY, menuList);
        return menuList;
    }

    @Override
    public UserMenuDTO selectMenuByMenuCd(String menuCd) {
        return userMenuMapper.selectMenuByMenuCd(menuCd);
    }

    @Override
    public UserMenuDTO selectBoardMenuDetail(BoardRequestDto boardRequestDto) {
        return userMenuMapper.selectMenuByBoardInfo(boardRequestDto);
    }

    @Override
    public List<UserMenuDTO> selectRootMenuList() {
        List<UserMenuDTO> cachedList = menuCache.getIfPresent(ROOT_MENU_LIST_CACHE_KEY);
        if (cachedList != null) {
            log.debug("Serving root menu list from cache");
            return cachedList;
        }

        log.debug("Fetching root menu list from database");
        List<UserMenuDTO> rootMenuList = userMenuMapper.selectRootMenuList();
        menuCache.put(ROOT_MENU_LIST_CACHE_KEY, rootMenuList);
        return rootMenuList;
    }

    @Override
    public UserMenuDTO selectValidBoardMenu(String menuCd, Long bbsOid) {
        return userMenuMapper.selectValidBoardMenu(menuCd, bbsOid);
    }

    @Override
    public void evictMenuCache() {
        menuCache.invalidateAll();
        log.info("Menu cache evicted");
    }
}
