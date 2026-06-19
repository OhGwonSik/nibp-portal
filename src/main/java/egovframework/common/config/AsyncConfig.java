package egovframework.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 처리 설정
 */
@Configuration
public class AsyncConfig {

    /**
     * 기본 비동기 실행자 (로그인 로그 기록 등)
     * @return
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);                    // 기본 스레드 수
        executor.setMaxPoolSize(10);                    // 최대 스레드 수
        executor.setQueueCapacity(100);                 // 대기 큐 크기
        executor.setThreadNamePrefix("async-");          // 스레드 이름 prefix
        executor.setWaitForTasksToCompleteOnShutdown(true);  // 종료 시 작업 완료 대기
        executor.setAwaitTerminationSeconds(60);         // 종료 대기 시간
        executor.initialize();
        return executor;
    }

    /**
     * 이메일 비동기 실행자 (서버 부하 고려)
     * @return
     */
    @Bean(name = "asyncEmailExecutor")
    public Executor asyncEmailExecutor() {ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 서버 부하를 고려해 동시 작업 수를 최소한으로 설정
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);    // 동시에 5개 이상의 연결을 맺지 않음
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("Internal-Mail-");

        // 큐가 가득 찼을 때: 호출한 스레드가 직접 처리하게 하여 속도를 늦춤 (Backpressure)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}