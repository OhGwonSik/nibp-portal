package egovframework.common.search.controller;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.common.api.ApiResponse;
import egovframework.common.search.domain.IntegratedSearchRequest;
import egovframework.common.search.service.IntegratedSearchService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/search")
public class IntegratedSearchController {
    private final IntegratedSearchService integratedSearchService;
    
    // 2글자 이상으로 조건 
    @PostMapping("/integrate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchIntegrated(@RequestBody @Valid IntegratedSearchRequest request) {
    	Map<String, Object> integratedSearchList = integratedSearchService.getIntegratedSearchData(request);
		return new ResponseEntity<>(ApiResponse.success(integratedSearchList), HttpStatus.OK);
    }
}
