package egovframework.common.search.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.search.component.NgramQueryComponent;
import egovframework.common.search.domain.IntegratedSearchRequest;
import egovframework.common.search.domain.IntegratedSearchResultDTO;
import egovframework.common.search.mapper.IntegratedSearchMapper;
import egovframework.common.search.service.IntegratedSearchService;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntegratedSearchServiceImpl extends EgovAbstractServiceImpl implements IntegratedSearchService {
	private final IntegratedSearchMapper integratedSearchMapper;
	
	// bbsPstOid : board_post , qna , faq , cardNews / noticeNo : notice 
	@Override
	public Map<String, Object> getIntegratedSearchData(IntegratedSearchRequest integratedSearchRequest) {
		// 검색어 유효성 검사 및 키워드 생성
	    String keyword = integratedSearchRequest.getKeyword();
	    if (keyword == null || keyword.trim().length() < 2) {
	        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "검색어는 2글자 이상 입력해주세요.");
	    }
		// 단어분할 미사용으로 주석
	    // String ngramKeyword = NgramQueryComponent.generateNgramQuery(keyword);
		String ngramKeyword = keyword;

	    Map<String, Object> resultMap = new HashMap<>();

	    if (integratedSearchRequest.getRootMenuNo() == null) {
	        /* 초기 통합검색 (대메뉴별 3건씩) */
	        List<IntegratedSearchResultDTO> allRows = integratedSearchMapper.selectInitialSearchList(
	                ngramKeyword, integratedSearchRequest.getSort(), keyword
	        );
	        allRows.forEach(item -> {
	            this.summarizeContent(item);
	            this.setDetailUrl(item);
	        });

	        List<Map<String, Object>> menuGroups = allRows.stream()
	            .filter(item -> item.getRootMenuNo() != null)
	            .collect(Collectors.groupingBy(IntegratedSearchResultDTO::getRootMenuNo))
	            .values().stream()
	            .map(list -> {
	                IntegratedSearchResultDTO first = list.get(0);
	                
	                PageInfo<IntegratedSearchResultDTO> pageInfo = new PageInfo<>(list);
	                pageInfo.setTotal(first.getGroupTotalCnt());
	                pageInfo.setPageNum(1);
	                pageInfo.setPageSize(3);
	                pageInfo.setHasNextPage(first.getGroupTotalCnt() > 3);

	                Map<String, Object> group = new HashMap<>();
	                group.put("rootMenuNo", first.getRootMenuNo());
	                group.put("rootMenuNm", first.getRootMenuNm());
	                group.put("pageInfo", pageInfo);
	                return group;
	            })
	            .collect(Collectors.toList());

	        int globalTotalCount = menuGroups.stream()
	            .mapToInt(group -> (int) ((PageInfo<?>) group.get("pageInfo")).getTotal())
	            .sum();
	        
	        resultMap.put("type", "INITIAL");
	        resultMap.put("menuGroups", menuGroups);
	        resultMap.put("totalSearchCount", globalTotalCount);

	    } else {
	    	
	        int offset = integratedSearchRequest.getPageNum();
	        int limit = integratedSearchRequest.getPageCnt();
	        
	        PageHelper.offsetPage(offset, limit);
	        
	        List<IntegratedSearchResultDTO> searchList = integratedSearchMapper.selectMoreSearchList(
	                ngramKeyword, integratedSearchRequest.getSort(),
	                keyword, integratedSearchRequest.getRootMenuNo()
	        );

	        searchList.forEach(item -> {
	            this.summarizeContent(item);
	            this.setDetailUrl(item);
	        });

	        PageInfo<IntegratedSearchResultDTO> pageInfo = new PageInfo<>(searchList);
	        
	        resultMap.put("type", "MORE");
	        resultMap.put("pageInfo", pageInfo);
	    }

	    return resultMap;
	}

	/**
	 * 본문 내용 50자 요약 메서드
	 */
	private void summarizeContent(IntegratedSearchResultDTO item) {
	    if (item.getContent() != null) {
	        String cleanText = item.getContent().trim();
	        if (cleanText.length() > 50) {
	            item.setContent(cleanText.substring(0, 50) + "...");
	        }
	    }
	}
	
	/**
	 * 상세 페이지 URL 생성 로직
	 */
	private void setDetailUrl(IntegratedSearchResultDTO item) {
	    String url = "";
	    String type = item.getBbsSeCd() != null ? item.getBbsSeCd() : "";

	    switch (type) {
	        case "POST": // 일반 게시판
	            url = "/board/post?bbsOid=" + item.getBbsOid() + "&menuCd=" + item.getMenuCd()+ "&bbsPstOid=" + item.getPostId();
	            break;
	        case "NOTICE": // 공지사항
	            url = "/board/notice/post?ntcOid=" + item.getPostId();
	            break;
	        case "QNA": // Q&A
	            url = "/board/qna/post?qnaOid=" + item.getPostId();
	            break;
	        case "FAQ": // FAQ
	            url = "/board/faq";
	            break;
	        case "CARD_NEWS": // 카드뉴스 
	            url = "#";
	            break;
	        default:
	            url = "#";
	    }
	    item.setViewUrl(url);
	}
}