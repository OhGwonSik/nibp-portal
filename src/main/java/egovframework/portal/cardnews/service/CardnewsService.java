package egovframework.portal.cardnews.service;

import com.github.pagehelper.PageInfo;
import egovframework.portal.cardnews.dto.CardnewsDTO;
import egovframework.portal.cardnews.dto.CardnewsFilter;

public interface CardnewsService {
    /**
     * 카드뉴스 목록 조회 (페이징 처리)
     */
    PageInfo<?> selectCardnewsPostListWithFilter(CardnewsFilter filter);

    /**
     * 카드뉴스 상세 조회
     */
    CardnewsDTO selectCardnewsById(Long cardNewsOid) ;
}