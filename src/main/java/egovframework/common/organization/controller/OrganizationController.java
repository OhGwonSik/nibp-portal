package egovframework.common.organization.controller;

import egovframework.common.api.ApiResponse;
import egovframework.common.organization.dto.OrganizationDTO;
import egovframework.common.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @ClassName : OrganizationController.java
 * @Description : 조직도 컨트롤러
 *
 * @author : j.h.kim
 * @since  : 2025. 12. 30
 * @version : 1.0
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/common/organization")
public class OrganizationController {
    
    private final OrganizationService organizationService;
    

    /**
     * 조직도 데이터 조회 API
     */
    @GetMapping("/tree")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<OrganizationDTO>>> getOrgChartTree() {
        List<OrganizationDTO> orgTree = organizationService.getOrgChartTree();
        return ResponseEntity.ok(ApiResponse.success(orgTree));
    }
}
