package egovframework.portal.notice.mapper;

import egovframework.portal.notice.dto.NoticeDTO;
import egovframework.portal.notice.dto.NoticeFilter;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface NoticeMapper {

    /**
     * notice 게시판 게시글 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return List<NoticeDTO> 게시글 목록
     */
    List<NoticeDTO> selectNoticePostListWithFilter(NoticeFilter filter);

    /**
     * notice 게시판 게시글 단건 조회
     * @param id 검색조건(공지 id)
     * @return NoticeDTO 게시글
     */
    NoticeDTO selectNoticeById(Long id);

    /**
     * notice 게시판 이전 게시글 조회
     * @param id 검색조건(공지 id)
     * @return NoticeDTO 이전 게시글
     */
    NoticeDTO selectPrevNotice(Long id);

    /**
     * notice 게시판 다음 게시글 조회
     * @param id 검색조건(공지 id)
     * @return NoticeDTO 다음 게시글
     */
    NoticeDTO selectNextNotice(Long id);

    /**
     * notice 게시판 조회수 증가
     * @param noticeDTO 공지 정보
     */
    void updateNoticeInqCnt(NoticeDTO noticeDTO);
}
