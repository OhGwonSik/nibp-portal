package egovframework.admin.admin1000.service;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin1000.domain.*;

import java.util.List;

/**
 * @ClassName : Admin1002Service.java
 * @Description : 부서 구성원 관리 Service
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
public interface Admin1002Service {
    PageInfo<Admin1002VO> selectAdmin1002List(Admin1002FilterDTO filter);
    Admin1002VO selectAdmin1002(Long deptMmbrOid);
    void insertAdmin1002(Admin1002DTO dto);
    void updateAdmin1002(Admin1002DTO dto);
    void deleteAdmin1002(Admin1002DeleteDTO dto);
    void updateDisplayOrder(Admin1002DTO dto);
}
