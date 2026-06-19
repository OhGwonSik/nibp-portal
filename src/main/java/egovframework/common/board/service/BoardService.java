package egovframework.common.board.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;

import egovframework.common.board.domain.BoardDataSetResponseDTO;
import egovframework.common.board.domain.BoardFilter;
import egovframework.common.board.domain.BoardRequestDto;
import egovframework.common.board.dto.BoardCommentDTO;
import egovframework.common.board.dto.BoardDTO;
import egovframework.common.board.dto.BoardPostDTO;
import egovframework.common.board.dto.BoardPostInsertDTO;
import egovframework.common.board.dto.BoardPostSatisfactionDTO;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.file.domain.FileDTO;

public interface BoardService {

    /**
     * 동적 게시판 게시글 목록 조회
     *
     * @param boardRequestDto BoardRequestDto
     */
    BoardDTO selectBoard(BoardRequestDto boardRequestDto);

    /**
     * 동적 게시판 여러 게시판 데이터 일괄 조회
     *
     * @param requests 게시판 요청 목록
     * @return List<BoardDataSetResponseDTO> 게시판별 데이터 (key, board, list 포함)
     */
    List<BoardDataSetResponseDTO> selectBoardsBySubBoardType(List<BoardRequestDto> requests);

    /**
     * 동적 게시판 게시글 목록 조회
     *
     * @param boardRequestDto BoardRequestDto
     * @param filter BoardFilter
     */
    PageInfo<?> selectBoardPostListWithFilter(BoardRequestDto boardRequestDto, BoardFilter filter);

    /**
     * 동적 게시판 게시글 단건 상세 조회
     *
     * @param boardRequestDto BoardRequestDto
     */
    BoardPostDTO selectBoardPostDetail(BoardRequestDto boardRequestDto);

    /**
     * 동적 게시판 게시글 단건 상세 조회 (ID로)
     *
     * @param bbsPstOid 게시글 번호
     * @return BoardPostDTO
     */
    BoardPostDTO selectBoardPostById(Long bbsPstOid);

    /**
     * 동적 게시판 다음 글 조회
     *
     * @param boardPostDTO BoardPostDTO
     */
    BoardPostDTO selectNextBoardPost(BoardPostDTO boardPostDTO);

    /**
     * 동적 게시판 이전 글 조회
     *
     * @param boardPostDTO BoardPostDTO
     */
    BoardPostDTO selectPrevBoardPost(BoardPostDTO boardPostDTO);

    /**
     * 동적 게시판 게시글 저장 (파일 업로드, 캡차 지원)
     *
     * @param boardPostInsertDTO BoardPostInsertDTO
     * @param files 첨부파일 목록
     * @param thumbnailFile 첨부파일 목록
     */
    void insertBoardPost(BoardPostInsertDTO boardPostInsertDTO, List<MultipartFile> files, MultipartFile thumbnailFile);
    
    /**
     * 동적 QNA 게시글 저장 - 어드민 (파일 업로드)
     *
     * @param boardPostInsertDTO BoardPostInsertDTO
     * @param files 첨부파일 목록
     */
    void insertBoardPostQNA(BoardPostInsertDTO boardPostInsertDTO, List<MultipartFile> files);
    
    /**
     * 동적 게시판 게시글 수정 (파일 업로드, 캡차 지원)
     *
     * @param boardPostInsertDTO BoardPostInsertDTO
     * @param files 첨부파일 목록
     * @param thumbnailFile 첨부파일 목록
     */
    void updateBoardPost(BoardPostInsertDTO boardPostInsertDTO, List<MultipartFile> files, MultipartFile thumbnailFile);
    
    /**
     * 동적 QNA 게시글 수정 (파일 업로드)
     *
     * @param boardPostInsertDTO BoardPostInsertDTO
     * @param files 첨부파일 목록
     */
    void updateBoardPostQNA(BoardPostInsertDTO boardPostInsertDTO, List<MultipartFile> files);
    

    /**
     * 동적 게시판 댓글 목록 조회
     *
     * @param boardPostDTO BoardPostDTO
     */
    List<BoardCommentDTO> selectBoardCommentList(BoardPostDTO boardPostDTO);

    /**
     * 동적 게시판 댓글 저장
     *
     * @param boardCommentDTO BoardCommentDTO
     */
    int insertBoardComment(BoardCommentDTO boardCommentDTO);
    
    /**
     * 동적 게시판 댓글 삭제
     * */
    int deleteBoardComment(BoardPostDTO boardPostDTO);

    /**
     * 동적 게시판 첨부파일 조회
     *
     * @param fileDTO FileDTO
     */
    List<FileDTO> selectBoardAttachList(FileDTO fileDTO);

    /**
     * 동적 게시판 조회수 업데이트
     *
     * @param boardPostDTO 게시글 정보
     */
    void updateBoardViewCnt(BoardPostDTO boardPostDTO);

    /**
     * 동적 게시판 비밀번호 확인
     *
     * @param bbsPstOid 게시글 번호
     * @param password 비밀번호
     * @return Boolean
     */
    Boolean checkBoardPassword(Long bbsPstOid, String password);
    
    /**
     *
     * */
    int deleteBoardPostAndComment(List<BoardPostDTO> boardPostDTO);

    /**
     * 게시글 답변 존재 여부 확인
     * @param bbsPstOid 게시글 번호
     * @return 답변이 있으면 true
     */
    boolean hasReply(Long bbsPstOid);

    Map<String, Object> getAdminDashboardModel();

    int insertSatisfactionByQnaBoardPost(BoardPostSatisfactionDTO boardPostSatisfactionDTO);

    /**
     * 메인 페이지 전용: 게시판 타입별 게시글 목록 조회 (하드코딩된 subBoardType 사용)
     *
     * @param subBoardType 게시판 서브타입
     * @param category 카테고리 (nullable)
     * @return BoardDataSetResponseDTO 게시판 정보와 게시글 목록
     */
    BoardDataSetResponseDTO selectMainPageBoardList(String subBoardType, String category);
    
    /**
     * 생명윤리 QNA 엑셀 다운로드
     *
     * @param BoardRequestDto
     * @return ExcelExportResult
     */
    public ExcelExportResult boardQnaExportExcel(BoardRequestDto boardRequestDto) throws IOException;
}
