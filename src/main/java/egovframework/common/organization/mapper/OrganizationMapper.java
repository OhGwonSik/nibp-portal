package egovframework.common.organization.mapper;

import egovframework.common.organization.dto.OrganizationDTO;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

/**
 * @ClassName : OrganizationMapper.java
 * @Description : 조직도 매퍼
 *
 * @author : j.h.kim
 * @since  : 2025. 12. 30
 * @version : 1.0
 */
@Mapper("organizationMapper")
public interface OrganizationMapper {
    
    /**
     * 전체 조직도 조회 (부서 + 멤버)
     * @param aesKey AES 암호화 키
     * @return List<OrganizationDTO> 조직도 목록
     */
    List<OrganizationDTO> selectOrgChart(@Param("aesKey") String aesKey);
}
