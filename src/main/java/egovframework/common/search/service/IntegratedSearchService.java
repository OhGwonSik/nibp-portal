package egovframework.common.search.service;

import java.util.Map;

import egovframework.common.search.domain.IntegratedSearchRequest;

public interface IntegratedSearchService {
	public Map<String, Object> getIntegratedSearchData(IntegratedSearchRequest integratedSearchRequest);
}
