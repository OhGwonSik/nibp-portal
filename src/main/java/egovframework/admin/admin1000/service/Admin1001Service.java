package egovframework.admin.admin1000.service;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin1000.domain.Admin1001DeleteDTO;
import egovframework.admin.admin1000.domain.Admin1001DTO;
import egovframework.admin.admin1000.domain.Admin1001FilterDTO;
import egovframework.admin.admin1000.domain.Admin1001VO;

import java.util.List;

/**
 * @ClassName : Admin1001Service.java
 * @Description : 부서 관리 Service
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
public interface Admin1001Service {
    PageInfo<Admin1001VO> selectAdmin1001List(Admin1001FilterDTO filter);
    Admin1001VO selectAdmin1001(Long deptOid);
    List<Admin1001VO> selectAdmin1001Tree();
    void insertAdmin1001(Admin1001DTO dto);
    void updateAdmin1001(Admin1001DTO dto);
    void deleteAdmin1001(Admin1001DeleteDTO dto);
    void updateSortSeq(Admin1001DTO dto);
}
