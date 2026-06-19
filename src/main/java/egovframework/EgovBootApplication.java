package egovframework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling // 스케줄링
@EnableAsync // 비동기 처리
@EnableAspectJAutoProxy // AOP
@EnableCaching // Spring cache
@ServletComponentScan
@SpringBootApplication
public class EgovBootApplication {
	public static void main(String[] args) {
		log.debug("##### EgovBootApplication Start #####");

		System.setProperty("server.servlet.encoding.charset", "UTF-8");
		System.setProperty("server.servlet.encoding.force", "true");
		System.setProperty("server.servlet.encoding.enabled", "true");

		SpringApplication springApplication = new SpringApplication(EgovBootApplication.class);
		springApplication.run(args);

		log.debug("##### EgovBootApplication End #####");
	}

}
