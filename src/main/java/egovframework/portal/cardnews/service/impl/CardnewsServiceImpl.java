package egovframework.portal.cardnews.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.common.file.domain.FileDTO;
import egovframework.common.file.mapper.FileMapper;
import egovframework.portal.cardnews.dto.CardnewsDTO;
import egovframework.portal.cardnews.dto.CardnewsFilter;
import egovframework.portal.cardnews.mapper.CardnewsMapper;
import egovframework.portal.cardnews.service.CardnewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardnewsServiceImpl extends EgovAbstractServiceImpl implements CardnewsService {
    private final CardnewsMapper cardnewsMapper;
    private final FileMapper fileMapper;

    @Override
    public PageInfo<?> selectCardnewsPostListWithFilter(CardnewsFilter filter) {
        if (filter != null && filter.getPage() != null && filter.getSize() != null) {
            PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
        }
        List<?> cardnewsPostList = cardnewsMapper.selectCardnewsPostListWithFilter(filter);

        return new PageInfo<>(cardnewsPostList);
    }

    @Override
    public CardnewsDTO selectCardnewsById(Long cardNewsOid) {
        if (cardNewsOid == null) {
            throw new IllegalArgumentException("카드뉴스 ID는 필수입니다.");
        }
        CardnewsDTO cardnewsPost = cardnewsMapper.selectCardnewsById(cardNewsOid);

        // 해당 카드뉴스 첨부 파일 목록 조회 (file 테이블에서)
        EgovMap egovMap = new EgovMap();
        egovMap.put("tblNm", "card_news");      // 어떤 테이블의 첨부 파일인지
        egovMap.put("tblOid", cardNewsOid);                  // 어떤 테이블의 어떤 pk의 첨부 파일인지
        List<FileDTO> attachFileList = fileMapper.selectAttachmentFileByTableNameAndTablePk(egovMap);

        // 해당 카드뉴스 첨부 파일 목록 DTO에 추가
        cardnewsPost.setAttachments(attachFileList);

        // 이전글/다음글 조회
        CardnewsDTO prevCardnews = cardnewsMapper.selectPrevCardnews(cardNewsOid);
        CardnewsDTO nextCardnews = cardnewsMapper.selectNextCardnews(cardNewsOid);

        cardnewsPost.setPrevCardnews(prevCardnews);
        cardnewsPost.setNextCardnews(nextCardnews);

        return cardnewsPost;
    }
}
