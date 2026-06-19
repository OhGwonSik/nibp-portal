package egovframework.common.code.mapper;

import egovframework.common.code.domain.CodeResponseDTO;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface CodeMapper {

    /**
     * group_code_no로 공통 코드 조회
     */
    List<CodeResponseDTO> selectCodeListByGrpCdOid(@Param("grpCdOid") Integer grpCdOid);

    /**
     * group_cd로 공통 코드 조회
     */
    List<CodeResponseDTO> selectGroupCodeListByGrpCd(String grpCd);
}