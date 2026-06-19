package egovframework.common.excel.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@ToString
@Component
@PropertySource("classpath:excel-download.properties")
@ConfigurationProperties(prefix = "excel-download")
public class ExcelConfig {

	private String template;
	
	private Map<String, ExcelPageConfigDTO> pages;
}
