package egovframework.common.organization.service;

import egovframework.common.organization.dto.OrganizationDTO;

import java.util.List;

/**
 * @ClassName : OrganizationService.java
 * @Description : 조직도 서비스 인터페이스
 *
 * @author : j.h.kim
 * @since  : 2025. 12. 30
 * @version : 1.0
 */
public interface OrganizationService {
    
    /**
     * 조직도 트리 구조로 조회
     * @return List<OrganizationDTO> 최상위 부서 목록 (children에 하위 부서 포함)
     */
    List<OrganizationDTO> getOrgChartTree();
}
