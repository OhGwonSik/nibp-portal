package egovframework.admin.admin800.mapper;

import egovframework.admin.admin800.domain.Admin805VO;
import egovframework.admin.admin800.domain.Admin805filterDto;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface Admin805Mapper {
	List<Admin805VO> selectPermissionChangeLogWithFilter(Admin805filterDto filter);
}
