package egovframework.common.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;

/**
 * @ClassName : DataSourceConfig.java
 * @Description : DataSource 및 MyBatis 설정
 *
 * @author : tspark
 * @since  : 2025. 11. 04
 * @version : 1.0
 */
@Slf4j
@Configuration
@MapperScan(
    basePackages = {
        "egovframework.**.mapper"  // 모든 하위 패키지의 mapper 스캔
    },
    annotationClass = Mapper.class, // 전자정부 프레임워크 mapper annotation
    sqlSessionFactoryRef = "sqlSessionFactory"
)
public class DataSourceConfig {

	@Value("${mybatis.schema:konibp_db}")
	private String schema;
	
	// mappers
	private final String[] mapperLocations = {
			"classpath:/egovframework/mapper/**/*.xml"
	};

	/**
	 * DataSource Bean 생성
	 * application.yml의 spring.datasource 설정을 자동으로 읽어옴
	 *
	 * Spring Boot가 자동으로 DataSource를 생성하므로 주석 처리
	 * 필요시 커스텀 설정을 위해 활성화
	 */
	// @Bean
	// @ConfigurationProperties(prefix = "spring.datasource.hikari")
	// public DataSource dataSource() {
	// 	return DataSourceBuilder.create().build();
	// }

	@Bean(name = {"sqlSessionFactory", "egov.sqlSessionFactory"})
	public SqlSessionFactory sqlSessionFactory(
			DataSource dataSource,
			org.apache.ibatis.session.Configuration mybatisConfiguration) throws Exception {
		log.info("Initializing SqlSessionFactory with Java Config...");

		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);

		try {
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

			// MyBatis Java Config 설정 (mapper-config.xml 대신 사용)
			sqlSessionFactoryBean.setConfiguration(mybatisConfiguration);
			log.info("MyBatis Configuration set from Java Config (MyBatisConfig.java)");

			// MyBatis 변수 설정 (SQL에서 ${schema} 사용 가능)
			// 환경별로 다른 스키마 사용 가능하도록 외부화 필요
			java.util.Properties properties = new java.util.Properties();
			properties.setProperty("schema", schema);
			sqlSessionFactoryBean.setConfigurationProperties(properties);
			log.info("MyBatis variables set: schema={}", schema);

			// Mapper XML 파일들 로드 (config 디렉토리 제외)
			Resource[] mapperResources = Arrays.stream(mapperLocations)
					.flatMap(location -> {
						try {
							Resource[] resources = resolver.getResources(location);
							if (resources.length > 0) {
								log.info("Loaded {} mapper files from: {}", resources.length, location);
							}
							return Arrays.stream(resources);
						} catch (IOException e) {
							log.error("Failed to load mapper files from: {}", location, e);
							throw new RuntimeException("매퍼 파일을 로드하는데 실패했습니다: " + location, e);
						}
					})
					// mapper-config.xml 제외 (더 이상 사용하지 않음)
					.filter(resource -> {
						try {
							String filename = resource.getFilename();
							boolean isMapperFile = filename != null && !filename.equals("mapper-config.xml");
							if (!isMapperFile) {
								log.debug("Excluding config file from mapper locations: {}", filename);
							}
							return isMapperFile;
						} catch (Exception e) {
							return true;
						}
					})
					.toArray(Resource[]::new);

			if (mapperResources.length > 0) {
				sqlSessionFactoryBean.setMapperLocations(mapperResources);
				log.info("Total {} mapper files loaded", mapperResources.length);
			} else {
				log.warn("No mapper XML files found. Check mapper-locations configuration.");
			}

		} catch (Exception e) {
			log.error("Failed to initialize SqlSessionFactory", e);
			throw new RuntimeException("SqlSessionFactory 초기화 실패", e);
		}

		return sqlSessionFactoryBean.getObject();
	}

	@Bean
	public SqlSessionTemplate egovSqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
