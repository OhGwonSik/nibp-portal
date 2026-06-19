package egovframework.portal.periodical.service;

import com.github.pagehelper.PageInfo;
import egovframework.portal.periodical.dto.Portal502DetailDTO;
import egovframework.portal.periodical.dto.Portal502Filter;
import egovframework.portal.periodical.dto.Portal502VO;

/**
 * @ClassName : Portal502Service.java
 * @Description : 정기발간자료 Service Interface
 *
 * @author : j.h.kim
 * @since : 2025. 01. 13
 * @version : 1.0
 */
public interface Portal502Service {
    
    /**
     * 정기발간자료 목록 조회
     */
    PageInfo<Portal502VO> selectPeriodicalList(Portal502Filter filter);
    
    /**
     * 정기발간자료 상세 조회
     */
    Portal502DetailDTO selectPeriodicalDetail(Long fxtmPblsDataOid);
}
