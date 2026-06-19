package egovframework.admin.admin1000.mapper;

import egovframework.admin.admin1000.domain.Admin1001DeleteDTO;
import egovframework.admin.admin1000.domain.Admin1001DTO;
import egovframework.admin.admin1000.domain.Admin1001FilterDTO;
import egovframework.admin.admin1000.domain.Admin1001VO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

/**
 * @ClassName : Admin1001Mapper.java
 * @Description : 부서 관리 Mapper
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
@Mapper("admin1001Mapper")
public interface Admin1001Mapper {
    List<Admin1001VO> selectAdmin1001List(Admin1001FilterDTO filter);
    Admin1001VO selectAdmin1001(Long deptOid);
    List<Admin1001VO> selectAdmin1001Tree();
    int insertAdmin1001(Admin1001DTO dto);
    int updateAdmin1001(Admin1001DTO dto);
    int deleteAdmin1001(Admin1001DeleteDTO dto);
    int checkChildDept(Long deptOid);
    int checkDeptMembers(Long deptOid);
    int updateSortSeq(Admin1001DTO dto);
}
