package egovframework.admin.admin700.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.admin.admin700.domain.Admin709FilterDTO;
import egovframework.admin.admin700.domain.EngagementStatisticsDTO;
import egovframework.admin.admin700.service.Admin709Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.enums.PermissionType;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin709")
public class Admin709Controller {
	private final Admin709Service admin709Service;
	
	
	@GetMapping("/engagement-statistics")
    @CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<EngagementStatisticsDTO>> selectEngagementStatistics(@LogParam @ModelAttribute Admin709FilterDTO filter) {
		EngagementStatisticsDTO result = admin709Service.selectEngagementStatistics(filter);
        return ResponseEntity.ok(ApiResponse.success(result));
	}
}
