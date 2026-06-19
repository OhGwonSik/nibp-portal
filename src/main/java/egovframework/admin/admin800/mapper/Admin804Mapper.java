package egovframework.admin.admin800.mapper;

import egovframework.admin.admin800.domain.Admin804filterDto;
import egovframework.common.audit.domain.ApiAccessLog;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface Admin804Mapper {
    List<ApiAccessLog> selectAccessLogWithFilter(@Param("filter") Admin804filterDto filter, @Param("aesKey") String aesKey);
}