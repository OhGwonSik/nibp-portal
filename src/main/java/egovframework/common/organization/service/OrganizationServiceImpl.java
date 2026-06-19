package egovframework.common.organization.service;

import egovframework.common.component.AESComponent;
import egovframework.common.organization.dto.OrganizationDTO;
import egovframework.common.organization.mapper.OrganizationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName : OrganizationServiceImpl.java
 * @Description : 조직도 서비스 구현체
 *
 * @author : j.h.kim
 * @since  : 2025. 12. 30
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    
    private final OrganizationMapper organizationMapper;
    private final AESComponent aesComponent;
    
    @Override
    public List<OrganizationDTO> getOrgChartTree() {
        // 1. 전체 부서와 멤버 조회 (복호화된 이메일 포함)
        List<OrganizationDTO> flatList = organizationMapper.selectOrgChart(aesComponent.getSecretKey());
        
        if (flatList == null || flatList.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 2. 중복 제거 및 멤버 병합 (MyBatis ResultMap이 중복 생성할 수 있음)
        Map<Long, OrganizationDTO> deptMap = new LinkedHashMap<>();
        
        for (OrganizationDTO dto : flatList) {
            Long deptOid = dto.getDeptOid();
            
            if (!deptMap.containsKey(deptOid)) {
                deptMap.put(deptOid, dto);
            } else {
                // 같은 부서가 있으면 멤버만 추가
                OrganizationDTO existing = deptMap.get(deptOid);
                if (dto.getMembers() != null && !dto.getMembers().isEmpty()) {
                    existing.getMembers().addAll(dto.getMembers());
                }
            }
        }
        
        // 3. 트리 구조로 변환
        List<OrganizationDTO> roots = new ArrayList<>();
        
        for (OrganizationDTO dept : deptMap.values()) {
            if (dept.getUpDeptOid() == null) {
                roots.add(dept);
            } else {
                OrganizationDTO parent = deptMap.get(dept.getUpDeptOid());
                if (parent != null) {
                    parent.getChildren().add(dept);
                }
            }
        }
        
        // 4. sortSeq 기준으로 정렬 (재귀적으로)
        sortTreeRecursively(roots);
        
        return roots;
    }
    
    /**
     * 트리 구조를 재귀적으로 정렬
     */
    private void sortTreeRecursively(List<OrganizationDTO> depts) {
        if (depts == null || depts.isEmpty()) {
            return;
        }
        
        // sortSeq 기준 정렬 (null은 0으로 처리)
        depts.sort(Comparator.comparingInt(d -> d.getSortSeq() != null ? d.getSortSeq() : 0));
        
        // 하위 부서도 재귀적으로 정렬
        for (OrganizationDTO dept : depts) {
            if (dept.getChildren() != null && !dept.getChildren().isEmpty()) {
                sortTreeRecursively(dept.getChildren());
            }
        }
    }
}
