package egovframework.common.config;

import egovframework.common.handler.EgovComTraceHandler;
import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.egovframe.rte.fdl.cmmn.trace.handler.TraceHandler;
import org.egovframe.rte.fdl.cmmn.trace.manager.DefaultTraceHandleManager;
import org.egovframe.rte.fdl.cmmn.trace.manager.TraceHandlerService;
import org.egovframe.rte.fdl.cryptography.EgovPasswordEncoder;
import org.egovframe.rte.fdl.cryptography.impl.EgovARIACryptoServiceImpl;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

@Configuration
@ComponentScan(basePackages = {
		"egovframework"
}, includeFilters = {
	@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Service.class),
	@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Mapper.class)
}, excludeFilters = {
	@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Controller.class),
	@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)
})
public class EgovCommonConfig {
    
	/**
	 * @return AntPathMatcher 등록.  Ant 경로 패턴 경로와 일치하는지 여부를 확인
	 */
	@Bean
	public AntPathMatcher antPathMatcher() {
		return new AntPathMatcher();
	}


	/**
	 * @return [LeaveaTrace 설정] defaultTraceHandler 등록
	 */
	@Bean
	public EgovComTraceHandler defaultTraceHandler() {
		return new EgovComTraceHandler();
	}

    /**
	 * @return [LeaveaTrace 설정] traceHandlerService 등록. TraceHandler 설정
	 */
	@Bean
	public DefaultTraceHandleManager traceHandlerService() {
		DefaultTraceHandleManager defaultTraceHandleManager = new DefaultTraceHandleManager();
		defaultTraceHandleManager.setReqExpMatcher(antPathMatcher());
		defaultTraceHandleManager.setPatterns(new String[] {"*"});
		defaultTraceHandleManager.setHandlers(new TraceHandler[] {defaultTraceHandler()});
		return defaultTraceHandleManager;
	}

	/**
	 * @return [LeaveaTrace 설정] LeaveaTrace 등록
	 */
	@Bean
	public LeaveaTrace leaveaTrace() {
		LeaveaTrace leaveaTrace = new LeaveaTrace();
		leaveaTrace.setTraceHandlerServices(new TraceHandlerService[] {traceHandlerService()});
		return leaveaTrace;
	}	
	
	/**
	 * 암복호화
	 * @return [EgovPasswordEncoder 설정] EgovPasswordEncoder 등록
	 */
	@Bean
	public EgovPasswordEncoder egovPasswordEncoder() {
		EgovPasswordEncoder egovPasswordEncoder = new EgovPasswordEncoder();
		egovPasswordEncoder.setAlgorithm("SHA-256");
		egovPasswordEncoder.setHashedPassword("gdyYs/IZqY86VcWhT8emCYfqY1ahw2vtLG+/FzNqtrQ=");
		return egovPasswordEncoder;
	}
	
	/**
	 * 암복호화
	 * @return [EgovARIACryptoServiceImpl 설정] EgovARIACryptoServiceImpl 등록
	 */
	@Bean
	public EgovARIACryptoServiceImpl egovARIACryptoService() {
		EgovARIACryptoServiceImpl egovARIACryptoServiceImpl = new EgovARIACryptoServiceImpl();
		egovARIACryptoServiceImpl.setPasswordEncoder(egovPasswordEncoder());
		egovARIACryptoServiceImpl.setBlockSize(1024);
		return egovARIACryptoServiceImpl;
	}
}