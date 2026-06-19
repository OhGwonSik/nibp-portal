package egovframework.admin.admin201.mapper;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.admin.admin201.domain.Admin201DTO;
import egovframework.admin.admin201.domain.Admin201FilterDTO;

@Mapper
public interface Admin201Mapper {
	
	List<Admin201DTO> selectOpinionList (Admin201FilterDTO admin201FilterDTO);
	
	Admin201DTO selectOpinionDetail(Admin201FilterDTO admin201FilterDTO);
}
