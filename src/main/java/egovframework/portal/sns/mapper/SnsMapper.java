package egovframework.portal.sns.mapper;

import egovframework.portal.sns.dto.SnsDTO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper("snsMapper")
public interface SnsMapper {

    /**
     * Sns 목록 조회
     */
    List<SnsDTO> selectSnsList();
}