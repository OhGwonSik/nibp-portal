package egovframework.portal.popup.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import egovframework.portal.popup.dto.PopupDTO;
import egovframework.portal.popup.mapper.PopupMapper;
import egovframework.portal.popup.service.PopupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopupServiceImpl extends EgovAbstractServiceImpl implements PopupService {
    private final PopupMapper popupMapper;

    private static final String POPUP_CACHE_KEY = "popupList";

    @Value("${app.cache.popup.expire-seconds:30}")
    private int popupCacheExpireSeconds;

    private Cache<String, List<PopupDTO>> popupCache;

    @PostConstruct
    public void init() {
        popupCache = Caffeine.newBuilder()
            .expireAfterWrite(popupCacheExpireSeconds, TimeUnit.SECONDS)
            .build();
    }

    @Override
    public List<PopupDTO> selectPopupList() {
        List<PopupDTO> cachedList = popupCache.getIfPresent(POPUP_CACHE_KEY);
        if (cachedList != null) {
            log.debug("Serving popup list from cache");
            return cachedList;
        }

        log.debug("Fetching popup list from database");
        List<PopupDTO> popupList = popupMapper.selectPopupList();
        popupCache.put(POPUP_CACHE_KEY, popupList);
        return popupList;
    }

    @Override
    public void evictPopupCache() {
        popupCache.invalidate(POPUP_CACHE_KEY);
        log.info("Popup cache evicted");
    }
}
