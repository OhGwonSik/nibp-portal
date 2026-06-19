package egovframework.common.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import egovframework.portal.mailing.service.MailingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
@RequiredArgsConstructor
public class MailingScheduler {
	private final MailingService mailingService;
	
	//@Scheduled(cron = "0 0 6 * * *")
	public void executeEmailAuthCleanup() {
		mailingService.executeEmailAuthCleanup();
	}
}
