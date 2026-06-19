package egovframework.common.board.mapper;

import egovframework.common.board.domain.BoardRequestDto;
import egovframework.common.board.dto.*;
import egovframework.common.file.domain.FileDTO;

import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface BoardMapper {

    /**
     * 동적 게시판 게시판 설정 정보 조회
     *
     * @param boardRequestDto BoardRequestDto
     */
    BoardDTO selectBoard(BoardRequestDto boardRequestDto);

    /**
     * 동적 게시판 subBoardType으로 게시판 설정 정보 조회
     *
     * @param boardRequestDto BoardRequestDto
     */
    BoardDTO selectBoardBySubBoardType(BoardRequestDto boardRequestDto);

    /**
     * 동적 게시판 게시글 목록 조회
     *
     * @param boardRequestDto BoardRequestDto
     */
    List<BoardPostDTO> selectBoardPostList(BoardRequestDto boardRequestDto);

    /**
     * 동적 게시판 게시글 목록 조회 - faq, qna
     *
     * @param boardRequestDto BoardRequestDto
     */
    List<BoardPostDTO> selectBoardPostListTypeB(BoardRequestDto boardRequestDto);
    
    /**
     * 동적 게시판 게시글 목록 조회 - 포토갤러리 게시판, 썸네일 게시판
     *
     * @param boardRequestDto BoardRequestDto
     */
    List<BoardPostDTO> selectBoardPostListTypeImage(BoardRequestDto boardRequestDto);

    /**
     * 대시보드 전용: 일반 게시글 목록 (뉴스 등) - N+1 없음
     *
     * @param boardRequestDto BoardRequestDto
     */
    List<BoardPostDTO> selectDashboardPostList(BoardRequestDto boardRequestDto);

    /**
     * 대시보드 전용: 갤러리 게시글 목록 - N+1 없음, 썸네일 서브쿼리 유지
     *
     * @param boardRequestDto BoardRequestDto
     */
    List<BoardPostDTO> selectDashboardPostListTypeImage(BoardRequestDto boardRequestDto);
    
    /**
     * 동적 게시판 게시글 단일 목록 조회 - 포토갤러리 게시판, 썸네일 게시판
     *
     * @param boardRequestDto BoardRequestDto
     */
    BoardPostDTO selectBoardPostTypeImage(BoardRequestDto boardRequestDto);

    /**
     * 동적 게시판 게시글 단건 상세 조회
     *
     * @param boardRequestDto BoardRequestDto
     */
    BoardPostDTO selectBoardPostDetail(BoardRequestDto boardRequestDto);

    /**
     * 동적 게시판 게시글 단건 상세 조회 (답변 포함 - QnA용)
     *
     * @param boardRequestDto BoardRequestDto
     */
    BoardPostDTO selectBoardPostDetailWithAnswer(BoardRequestDto boardRequestDto);

    /**
     * 동적 게시판 게시글 단건 상세 조회 (ID로)
     *
     * @param bbsPstOid 게시글 번호
     */
    BoardPostDTO selectBoardPostById(Long bbsPstOid);
    
    /**
     * 동적 게시판 게시글 비밀번호 체크
     *
     * @param bbsPstOid 게시글 
     * @param wrtrPswd 비밀번호
     */
    BoardPostDTO checkBoardPostPassword(@Param("bbsPstOid") Long bbsPstOid, @Param("wrtrPswd") String wrtrPswd);

    /**
     * 동적 게시판 다음 글 조회
     *
     * @param boardPostDTO 게시글 정보
     */
    BoardPostDTO selectNextBoardPost(BoardPostDTO boardPostDTO);

    /**
     * 동적 게시판 이전 글 조회
     *
     * @param boardPostDTO 게시글 정보
     */
    BoardPostDTO selectPrevBoardPost(BoardPostDTO boardPostDTO);

    /**
     * 동적 게시판 게시글 저장
     *
     * @param boardPostInsertDTO BoardPostInsertDTO
     */
    int insertBoardPost(BoardPostInsertDTO boardPostInsertDTO);
    
    /**
     * 동적 게시판 게시글 수정
     *
     * @param boardPostInsertDTO BoardPostInsertDTO
     */
    int updateBoardPost(BoardPostInsertDTO boardPostInsertDTO);
    
    /**
     * 동적 QNA 게시판 게시글 상태 수정
     *
     * @param boardPostInsertDTO BoardPostInsertDTO
     */
    int updateQnaBoardPostStatus(BoardPostInsertDTO boardPostInsertDTO);
    
	/**
	 * 동적 Q&A 비밀글 여부 확인
	 * @param boardPostInsertDTO Q&A 정보
	 * @return BoardPostDTO (게시판 서브타입 / 비밀글여부)
	 */
    BoardPostDTO selectBoardPostParentSecretYn(BoardPostInsertDTO boardPostInsertDTO);
	
	/**
	 * 동적 Q&A 만족도평가 insert : 디폴트 미응답
	 * @param boardPostInsertDTO Q&A 정보
	 * @return int
	 */
	int insertBoardPostQnaSatisfaction(BoardPostInsertDTO boardPostInsertDTO);    
    
    /**
     * 동적 게시판 댓글 목록 조회
     *
     * @param bbsPstOid 게시글 번호
     */
    List<BoardCommentDTO> selectBoardCommentList(Long bbsPstOid);

    /**
     * 동적 게시판 댓글 저장
     *
     * @param boardCommentDTO BoardCommentDTO
     */
    int insertBoardComment(BoardCommentDTO boardCommentDTO);

    /**
     * 동적 게시판 첨부파일 조회
     *
     * @param fileDTO 파일 정보
     */
    List<FileDTO> selectBoardAttachList(FileDTO fileDTO);

    /**
     * 동적 게시판 조회수 업데이트
     *
     * @param boardPostDTO 게시글 번호
     */
    int updateBoardViewCnt(BoardPostDTO boardPostDTO);
    
    /**
     * 동적 게시판 내 게시글 삭제 SOFT DELETE
     * @param boardPostDTO BoardPostDTO
     * */
    int deleteBoardPost(BoardPostDTO boardPostDTO);
    
    /**
     * 동적 게시판 내 게시글내 코멘트 삭제 SOFT DELETE
     * @param boardPostDTO BoardPostDTO
     * */
    int deleteBoardComment(BoardPostDTO boardPostDTO);

    /**
     * 게시글 답변 존재 여부 확인
     * @param bbsPstOid 게시글 번호
     * @return 답변 수
     */
    int countReplyByBbsPstOid(Long bbsPstOid);

    /**
     * 동적 Q&A 만족도 평가 여부 확인
     * @param boardPostSatisfactionDTO Q&A 정보
     */
    int selectSatisfactionCountByBbsPstOid(BoardPostSatisfactionDTO boardPostSatisfactionDTO);
    
    /**
     * 동적 Q&A 만족도 평가 저장되었는지 확인
     * @param boardPostSatisfactionDTO Q&A 정보
     */
    int selectValidSatisfactionExists(BoardPostSatisfactionDTO boardPostSatisfactionDTO);
    
    /**
     * 동적 Q&A 만족도 평가 저장
     * @param boardPostSatisfactionDTO Q&A 만족도 평가 정보
     */
    int updateSatisfactionCountByBbsPstOid(BoardPostSatisfactionDTO boardPostSatisfactionDTO);
    
    /**
     * 동적 생명윤리 Q&A 히스토리 저장
     * @param boardPostInsertDTO Q&A 정보
     */
    int insertUserBoardHistory(BoardPostInsertDTO boardPostInsertDTO);
    
    /**
     * 동적 생명윤리 Q&A 히스토리 중복확인
     * @param boardPostInsertDTO Q&A 정보
     */
    int selectUserBoardHistoryCount(BoardPostInsertDTO boardPostInsertDTO);

    /**
     * 메인 페이지 전용: 여러 게시글의 첨부파일 일괄 조회 (N+1 방지)
     * @param bbsPstOids 게시글 번호 목록
     */
    List<FileDTO> selectFirstPostAttachmentsByBbsPstOids(@org.apache.ibatis.annotations.Param("bbsPstOids") List<Long> bbsPstOids);
    
    /**
     * 동적 생명윤리 Q&A 엑셀 다운로드
     */
    List<BoardQnaExcelDTO> selectQnaDetailedStatList(BoardRequestDto boardRequestDto);
}
