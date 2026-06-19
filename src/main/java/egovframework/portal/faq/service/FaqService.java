package egovframework.portal.faq.service;

import com.github.pagehelper.PageInfo;
import egovframework.portal.faq.dto.FaqCategoryDTO;
import egovframework.portal.faq.dto.FaqFilter;

import java.util.List;

public interface FaqService {

    /**
     * Faq 카테고리 조회
     */
    List<FaqCategoryDTO> selectFaqCategoryList();

    /**
     * Faq 목록 조회 (페이징 처리)
     */
    PageInfo<?> selectFaqPostListWithFilter(FaqFilter filter);
}