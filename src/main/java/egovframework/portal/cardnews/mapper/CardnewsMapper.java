package egovframework.portal.cardnews.mapper;

import egovframework.portal.cardnews.dto.CardnewsDTO;
import egovframework.portal.cardnews.dto.CardnewsFilter;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface CardnewsMapper {

    /**
     * cardnews 게시판 게시글 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return List<PopupZoneDTO> 게시글 목록
     */
    List<CardnewsDTO> selectCardnewsPostListWithFilter(CardnewsFilter filter);

    /**
     * cardnews 게시판 게시글 단건 조회
     * @param id 검색조건(카드뉴스 id)
     * @return PopupZoneDTO 게시글
     */
    CardnewsDTO selectCardnewsById(Long id);

    /**
     * cardnews 게시판 이전 게시글 조회
     * @param id 검색조건(공지 id)
     * @return PopupZoneDTO 이전 게시글
     */
    CardnewsDTO selectPrevCardnews(Long id);

    /**
     * cardnews 게시판 다음 게시글 조회
     * @param id 검색조건(공지 id)
     * @return PopupZoneDTO 다음 게시글
     */
    CardnewsDTO selectNextCardnews(Long id);
}