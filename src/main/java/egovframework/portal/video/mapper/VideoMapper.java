package egovframework.portal.video.mapper;

import egovframework.portal.video.dto.VideoDTO;
import egovframework.portal.video.dto.VideoFilter;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper("videoMapper") // Apply the specified Mapper annotation
public interface VideoMapper {

    /**
     * video 게시판 게시글 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return List<VideoDTO> 게시글 목록
     */
    List<VideoDTO> selectVideoPostListWithFilter(VideoFilter filter);

    /**
     * video 게시판 게시글 단건 조회
     * @param id 검색조건(카드뉴스 id)
     * @return VideoDTO 게시글
     */
    VideoDTO selectVideoById(Long id);

    /**
     * video 게시판 이전 게시글 조회
     * @param id 검색조건(공지 id)
     * @return VideoDTO 이전 게시글
     */
    VideoDTO selectPrevVideo(Long id);

    /**
     * video 게시판 다음 게시글 조회
     * @param id 검색조건(공지 id)
     * @return VideoDTO 다음 게시글
     */
    VideoDTO selectNextVideo(Long id);
}