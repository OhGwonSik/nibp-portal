package egovframework.portal.faq.mapper;

import egovframework.portal.faq.dto.FaqCategoryDTO;
import egovframework.portal.faq.dto.FaqDTO;
import egovframework.portal.faq.dto.FaqFilter;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper("faqMapper")
public interface FaqMapper {

    /**
     * faq 게시판 카테고리 목록 조회
     * @return List<FaqCategoryDTO> 카테고리 목록
     */
    List<FaqCategoryDTO> selectFaqCategoryList();

    /**
     * faq 게시판 게시글 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return List<FaqDTO> 게시글 목록
     */
    List<FaqDTO> selectFaqPostListWithFilter(FaqFilter filter);
}