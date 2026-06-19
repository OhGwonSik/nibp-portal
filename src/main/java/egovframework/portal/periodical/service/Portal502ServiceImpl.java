package egovframework.portal.periodical.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.portal.periodical.dto.*;
import egovframework.portal.periodical.mapper.Portal502Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName : Portal502ServiceImpl.java
 * @Description : 정기발간자료 Service 구현체
 *
 * @author : j.h.kim
 * @since : 2025. 01. 13
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Portal502ServiceImpl implements Portal502Service {
    
    private final Portal502Mapper portal502Mapper;
    
    /**
     * 정기발간자료 목록 조회
     */
    @Override
    public PageInfo<Portal502VO> selectPeriodicalList(Portal502Filter filter) {
        PageHelper.startPage(filter.getPageNum(), filter.getPageCnt());
        List<Portal502VO> list = portal502Mapper.selectPeriodicalList(filter);
        return new PageInfo<>(list);
    }
    
    /**
     * 정기발간자료 상세 조회
     */
    @Override
    @Transactional
    public Portal502DetailDTO selectPeriodicalDetail(Long fxtmPblsDataOid) {
        // 상세 정보 조회
        Portal502DetailDTO detail = portal502Mapper.selectPeriodicalDetail(fxtmPblsDataOid);

        if (detail == null) {
            return null;
        }

        // 섹션 목록 조회
        List<Portal502SectionDTO> sections = portal502Mapper.selectPeriodicalSections(fxtmPblsDataOid);
        if (sections != null && !sections.isEmpty()) {
            // 각 섹션의 아이템 조회
            for (Portal502SectionDTO section : sections) {
                List<Portal502ItemDTO> items = portal502Mapper.selectPeriodicalItems(section.getFxtmPblsSectOid());
                section.setItems(items);
            }
        }
        detail.setSections(sections);

        // 첨부파일 목록 조회
        List<Portal502FileDTO> files = portal502Mapper.selectPeriodicalFiles(fxtmPblsDataOid);
        detail.setFiles(files);

        // 이전글/다음글 조회
        Portal502NavDTO prevPost = portal502Mapper.selectPrevPost(fxtmPblsDataOid);
        Portal502NavDTO nextPost = portal502Mapper.selectNextPost(fxtmPblsDataOid);
        detail.setPrevPost(prevPost);
        detail.setNextPost(nextPost);

        // 조회수 증가
        portal502Mapper.updateViewCnt(fxtmPblsDataOid);

        return detail;
    }
}
