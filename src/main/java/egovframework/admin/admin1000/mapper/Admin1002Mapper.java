package egovframework.admin.admin1000.mapper;

import egovframework.admin.admin1000.domain.*;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

/**
 * @ClassName : Admin1002Mapper.java
 * @Description : 부서 구성원 관리 Mapper
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 08
 * @version : 1.0
 */
@Mapper("admin1002Mapper")
public interface Admin1002Mapper {
    List<Admin1002VO> selectAdmin1002List(Admin1002FilterDTO filter);
    Admin1002VO selectAdmin1002(Long deptMmbrOid);
    int insertAdmin1002(Admin1002DTO dto);
    int updateAdmin1002(Admin1002DTO dto);
    int deleteAdmin1002(Admin1002DeleteDTO dto);
    int updateDisplayOrder(Admin1002DTO dto);
    int checkDuplicateMember(Admin1002DTO dto);
}
