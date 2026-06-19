package egovframework.portal.periodical.mapper;

import egovframework.portal.periodical.dto.*;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

/**
 * @ClassName : Portal502Mapper.java
 * @Description : 정기발간자료 Mapper
 *
 * @author : j.h.kim
 * @since : 2025. 01. 13
 * @version : 1.0
 */
@Mapper
public interface Portal502Mapper {
    
    /**
     * 정기발간자료 목록 조회
     */
    List<Portal502VO> selectPeriodicalList(Portal502Filter filter);
    
    /**
     * 정기발간자료 상세 조회
     */
    Portal502DetailDTO selectPeriodicalDetail(@Param("fxtmPblsDataOid") Long fxtmPblsDataOid);

    /**
     * 정기발간자료 섹션 목록 조회
     */
    List<Portal502SectionDTO> selectPeriodicalSections(@Param("fxtmPblsDataOid") Long fxtmPblsDataOid);

    /**
     * 정기발간자료 아이템 목록 조회
     */
    List<Portal502ItemDTO> selectPeriodicalItems(@Param("fxtmPblsSectOid") Long fxtmPblsSectOid);

    /**
     * 정기발간자료 첨부파일 목록 조회
     */
    List<Portal502FileDTO> selectPeriodicalFiles(@Param("fxtmPblsDataOid") Long fxtmPblsDataOid);

    /**
     * 이전글 조회
     */
    Portal502NavDTO selectPrevPost(@Param("fxtmPblsDataOid") Long fxtmPblsDataOid);

    /**
     * 다음글 조회
     */
    Portal502NavDTO selectNextPost(@Param("fxtmPblsDataOid") Long fxtmPblsDataOid);

    /**
     * 조회수 증가
     */
    void updateViewCnt(@Param("fxtmPblsDataOid") Long fxtmPblsDataOid);
}
