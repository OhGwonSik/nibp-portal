package egovframework.portal.notice.service;

import com.github.pagehelper.PageInfo;
import egovframework.portal.notice.dto.NoticeDTO;
import egovframework.portal.notice.dto.NoticeFilter;

public interface NoticeService {

    /**
     * 공지사항 목록 조회 (페이징 처리)
     */
    PageInfo<NoticeDTO> selectNoticePostListWithFilter(NoticeFilter filter);

    /**
     * 공지사항 상세 조회
     */
    NoticeDTO selectNoticeById(Long ntcOid);

    /**
     * notice 게시판 조회수 증가
     * @param noticeDTO 공지 정보
     */
    void updateNoticeInqCnt(NoticeDTO noticeDTO);
}
