package egovframework.admin.admin800.mapper;

import egovframework.admin.admin800.domain.Admin806VO;
import egovframework.admin.admin800.domain.Admin806filterDto;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface Admin806Mapper {
    /**
     * 개인정보 처리 로그 조회
     * @param filter 검색 조건
     * @return 개인정보 처리 로그 목록
     */
    List<Admin806VO> selectPersonalInfoProcLogWithFilter(@Param("filter") Admin806filterDto filter, @Param("aesKey") String aesKey);
}
