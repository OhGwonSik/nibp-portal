package egovframework.admin.admin201.service;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin201.domain.Admin201DTO;
import egovframework.admin.admin201.domain.Admin201FilterDTO;

public interface Admin201Service {
	
	PageInfo<Admin201DTO> selectOpinionList (Admin201FilterDTO admin201FilterDTO);
	
	Admin201DTO selectOpinionDetail(Admin201FilterDTO admin201FilterDTO);
}
