package egovframework.portal.sns.scheduler;

import egovframework.portal.sns.service.SnsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class YoutubeCacheScheduler {
    private final SnsService snsService;

    @PostConstruct
    public void warmUp() {
        log.info("YouTube 캐시 초기 워밍업 시작");
        try {
            snsService.refreshAllYoutubeCache();
        } catch (Exception e) {
            log.warn("YouTube 캐시 초기 워밍업 실패: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRateString = "#{${app.cache.youtube.refresh-minutes:300} * 60 * 1000}")
    public void refreshYoutubeCache() {
        log.info("YouTube 캐시 스케줄러 실행");
        try {
            snsService.refreshAllYoutubeCache();
        } catch (Exception e) {
            log.error("YouTube 캐시 스케줄러 실행 중 오류", e);
        }
    }
}
