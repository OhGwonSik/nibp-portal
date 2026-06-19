package egovframework.common.config;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * @ClassName : ThymeleafConfig.java
 * @Description : ThymeleafConfig Java Config 설정
 *
 * @author : tspark
 * @since  : 2025. 11. 04
 * @version : 1.0
 */
@Configuration
public class ThymeleafConfig {
    @Bean
    public LayoutDialect layoutDialect() { // thymeleaf layout view return 을 위한 추가
        return new LayoutDialect();
    }
}
