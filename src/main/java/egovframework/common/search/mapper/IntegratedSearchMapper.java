package egovframework.common.search.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.common.search.domain.IntegratedSearchResultDTO;

@Mapper
public interface IntegratedSearchMapper {
	
	/**
     * 초기 검색 (대메뉴별 상위 3건 요약)
     */
    List<IntegratedSearchResultDTO> selectInitialSearchList(
													        @Param("ngramKeyword") String ngramKeyword,
													        @Param("sort") String sort,
													        @Param("keyword") String keyword
    );

    /**
     * 더보기 검색 (특정 대메뉴 페이징 처리)
     */
    List<IntegratedSearchResultDTO> selectMoreSearchList(
												        @Param("ngramKeyword") String ngramKeyword,
												        @Param("sort") String sort,
												        @Param("keyword") String keyword,
												        @Param("rootMenuNo") Integer rootMenuNo
    );
}
