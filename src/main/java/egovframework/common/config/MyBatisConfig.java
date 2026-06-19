package egovframework.common.config;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.type.JdbcType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import egovframework.common.auth.domain.RefreshToken;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName : MyBatisConfig.java
 * @Description : MyBatis Java Config 설정
 *                application.yml의 mybatis.configuration 설정을 Java로 적용
 *
 * @author : tspark
 * @since  : 2025. 11. 04
 * @version : 1.0
 *
 * Note: Spring Boot의 mybatis.configuration 설정이 자동으로 적용되지 않는 환경에서
 *       수동으로 SqlSessionFactory를 생성하므로, yml의 설정을 Java Config로 적용
 */
@Slf4j
@Configuration
public class MyBatisConfig {

    /**
     * MyBatis Configuration Bean
     * application.yml의 mybatis.configuration 설정을 Java로 적용하고
     * Type Aliases를 등록
     */
    @Bean
    public org.apache.ibatis.session.Configuration mybatisConfiguration() {
        log.info("Initializing MyBatis Configuration...");

        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();

        // === application.yml의 mybatis.configuration 설정 적용 ===

        // map-underscore-to-camel-case: true
        configuration.setMapUnderscoreToCamelCase(true);
        log.debug("MyBatis setting: mapUnderscoreToCamelCase = true");

        // jdbc-type-for-null: VARCHAR
        configuration.setJdbcTypeForNull(JdbcType.VARCHAR);
        log.debug("MyBatis setting: jdbcTypeForNull = VARCHAR");

        // local-cache-scope: SESSION
        configuration.setLocalCacheScope(LocalCacheScope.SESSION);
        log.debug("MyBatis setting: localCacheScope = SESSION");

        // cache-enabled: true
        configuration.setCacheEnabled(true);
        log.debug("MyBatis setting: cacheEnabled = true");

        // default-executor-type: SIMPLE
        configuration.setDefaultExecutorType(ExecutorType.SIMPLE);
        log.debug("MyBatis setting: defaultExecutorType = SIMPLE");

        // default-fetch-size: 100
        configuration.setDefaultFetchSize(100);
        log.debug("MyBatis setting: defaultFetchSize = 100");

        // default-statement-timeout: 30
        configuration.setDefaultStatementTimeout(30);
        log.debug("MyBatis setting: defaultStatementTimeout = 30");

        // lazy-loading-enabled: false
        configuration.setLazyLoadingEnabled(false);
        log.debug("MyBatis setting: lazyLoadingEnabled = false");

        // aggressive-lazy-loading: false
        configuration.setAggressiveLazyLoading(false);
        log.debug("MyBatis setting: aggressiveLazyLoading = false");

        // log-prefix: "[SQL] "
        configuration.setLogPrefix("[SQL] ");
        log.debug("MyBatis setting: logPrefix = [SQL]");

        // default-enum-type-handler: org.apache.ibatis.type.EnumTypeHandler
        configuration.setDefaultEnumTypeHandler(org.apache.ibatis.type.EnumTypeHandler.class);
        log.debug("MyBatis setting: defaultEnumTypeHandler = EnumTypeHandler");

        // === Type Aliases 등록 ===

        // eGovFrame 기본 타입
        configuration.getTypeAliasRegistry().registerAlias("egovMap", org.egovframe.rte.psl.dataaccess.util.EgovMap.class);
        log.debug("MyBatis typeAlias registered: egovMap");

        // 프로젝트 도메인 타입
        configuration.getTypeAliasRegistry().registerAlias("RefreshToken", RefreshToken.class);
        log.debug("MyBatis typeAlias registered: RefreshToken");

        log.info("MyBatis Configuration initialized successfully");
        return configuration;
    }
}