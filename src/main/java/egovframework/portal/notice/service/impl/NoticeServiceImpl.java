package egovframework.portal.notice.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.common.file.domain.FileDTO;
import egovframework.common.file.mapper.FileMapper;
import egovframework.portal.notice.dto.NoticeDTO;
import egovframework.portal.notice.dto.NoticeFilter;
import egovframework.portal.notice.mapper.NoticeMapper;
import egovframework.portal.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends EgovAbstractServiceImpl implements NoticeService {
    private final NoticeMapper noticeMapper;
    private final FileMapper fileMapper;

    @Value("${app.cache.notice.expire-seconds:30}")
    private int noticeCacheExpireSeconds;

    @Value("${app.cache.notice.maximum-size:100}")
    private int noticeCacheMaximumSize;

    // 캐시 키: "page_size" 형태 (예: "1_10")
    private Cache<String, PageInfo<?>> noticeListCache;

    @PostConstruct
    public void init() {
        noticeListCache = Caffeine.newBuilder()
            .expireAfterWrite(noticeCacheExpireSeconds, TimeUnit.SECONDS)
            .maximumSize(noticeCacheMaximumSize)
            .build();
    }

    @Override
    public PageInfo<NoticeDTO> selectNoticePostListWithFilter(NoticeFilter filter) {
        // 메인 페이지 조회 (page=1, size가 작은 경우)만 캐싱
        boolean isCacheable = filter != null
            && filter.getPage() != null && filter.getPage() == 1
            && filter.getSize() != null && filter.getSize() <= 15
            && (filter.getKeyword() == null || filter.getKeyword().isEmpty());

        if (isCacheable) {
            String cacheKey = filter.getPage() + "_" + filter.getSize();
            PageInfo<NoticeDTO> cachedResult = (PageInfo<NoticeDTO>) noticeListCache.getIfPresent(cacheKey);
            if (cachedResult != null) {
                log.debug("Serving notice list from cache, key: {}", cacheKey);
                return cachedResult;
            }

            PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
            List<NoticeDTO> noticePostList = noticeMapper.selectNoticePostListWithFilter(filter);
            PageInfo<NoticeDTO> result = new PageInfo<>(noticePostList);
            populateAttachments(result.getList());

            noticeListCache.put(cacheKey, result);
            log.debug("Notice list cached, key: {}", cacheKey);
            return result;
        }

        // 캐싱 대상이 아닌 경우 (검색, 2페이지 이상 등)
        if (filter != null && filter.getPage() != null && filter.getSize() != null) {
            PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
        }
        List<NoticeDTO> noticePostList = noticeMapper.selectNoticePostListWithFilter(filter);
        PageInfo<NoticeDTO> result = new PageInfo<>(noticePostList);
        populateAttachments(result.getList());

        return result;
    }

    private void populateAttachments(List<NoticeDTO> noticeList) {
        if (noticeList == null || noticeList.isEmpty()) {
            return;
        }
        List<Long> ntcOids = noticeList.stream()
                .map(NoticeDTO::getNtcOid)
                .collect(Collectors.toList());

        EgovMap param = new EgovMap();
        param.put("tblNm", "ntc");
        param.put("tblOids", ntcOids);
        List<FileDTO> allFiles = fileMapper.selectAttachmentFilesByTableNameAndTablePks(param);

        Map<Long, List<FileDTO>> fileMap = allFiles.stream()
                .collect(Collectors.groupingBy(FileDTO::getTblOid));

        for (NoticeDTO notice : noticeList) {
            notice.setAttachments(fileMap.getOrDefault(notice.getNtcOid(), Collections.emptyList()));
        }
    }

    @Override
    public NoticeDTO selectNoticeById(Long ntcOid) {
        if (ntcOid == null) {
            throw new IllegalArgumentException("공지사항 ID는 필수입니다.");
        }
        NoticeDTO noticePost = noticeMapper.selectNoticeById(ntcOid);

        // 해당 공지사항 첨부 파일 목록 조회
        EgovMap egovMap = new EgovMap();
        egovMap.put("tblNm", "ntc");      // 어떤 테이블의 첨부 파일인지
        egovMap.put("tblOid", ntcOid);           // 어떤 테이블의 어떤 pk의 첨부 파일인지
        List<FileDTO> attachFileList = fileMapper.selectAttachmentFileByTableNameAndTablePk(egovMap);

        // 해당 공지사항 첨부 파일 목록 DTO에 추가
        noticePost.setAttachments(attachFileList);

        // 이전글/다음글 조회
        NoticeDTO prevNotice = noticeMapper.selectPrevNotice(ntcOid);
        NoticeDTO nextNotice = noticeMapper.selectNextNotice(ntcOid);

        noticePost.setPrevNotice(prevNotice);
        noticePost.setNextNotice(nextNotice);

        return noticePost;
    }

    @Override
    public void updateNoticeInqCnt(NoticeDTO noticeDTO) {
        noticeMapper.updateNoticeInqCnt(noticeDTO);
    }
}
