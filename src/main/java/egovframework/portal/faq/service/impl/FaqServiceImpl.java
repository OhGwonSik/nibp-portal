package egovframework.portal.faq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.portal.faq.dto.FaqCategoryDTO;
import egovframework.portal.faq.dto.FaqFilter;
import egovframework.portal.faq.mapper.FaqMapper;
import egovframework.portal.faq.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaqServiceImpl extends EgovAbstractServiceImpl implements FaqService {
    private final FaqMapper faqMapper;

    @Override
    public List<FaqCategoryDTO> selectFaqCategoryList() {
        return faqMapper.selectFaqCategoryList();
    }

    @Override
    public PageInfo<?> selectFaqPostListWithFilter(FaqFilter filter) {
        if(filter != null && filter.getPage() != null && filter.getSize() != null){
            PageHelper.startPage(filter.getPage(), filter.getSize());
        }

        return PageInfo.of(faqMapper.selectFaqPostListWithFilter(filter));
    }
}