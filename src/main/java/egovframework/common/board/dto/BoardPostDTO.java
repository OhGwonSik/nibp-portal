package egovframework.common.board.dto;

import egovframework.common.file.domain.FileDTO;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BoardPostDTO {

    private Long bbsPstOid; // 게시글번호
    private Long bbsOid; // 게시판번호
    private Long userOid; // 사용자번호
    private String menuCd;
    private Long bbsCmntOid; //코멘트 번호
    private String wrtrNm; // 작성자명
    private String wrtrPswd; // 작성자비밀번호
    private String bbsPstTtl; // 게시글제목
    private String bbsPstCn; // 게시글내용
    private String bbsPstCnTxt; // 게시글내용 (태그 제거)
    private String ansCn; // faq용 답변 내용 (CKEditor 등 HTML 저장)
    private String ctgry; // 분류
    private String ntcYn; // 공지여부
    private String prvtPstYn; // 비밀글여부
    private Integer inqCnt; // 조회수
    private Integer likeCnt; // 좋아요수
    private String upendFixYn; //상단 고정여부
    private LocalDate bgngDt; // 시작일
    private LocalDate endDt; // 종료일
    private Integer cmntCnt; // 댓글수
    private String thmbPath; // 썸네일 이미지 파일 경로(갤러리/썸네일용)
    private Integer atchCnt; // 첨부파일수
    private String stts; // 상태
    private LocalDate ddlnDt; // 마감일
    private Long upBbsPstOid; // 원글번호
    private Integer replyLv; // 답글레벨
    private Integer replySeq; // 답글순서
    private String delYn; // 삭제여부
    private LocalDateTime delDt; // 삭제일시
    private String openYn; // 공개여부
    private String rcrtType; // 모집유형
    private String regId; // 등록자ID
    private LocalDateTime regDt; // 등록일시
    private String mdfcnId; // 수정자ID
    private LocalDateTime mdfcnDt; // 수정일시
    private String kwrd; // 키워드

    private Integer fileCnt; //파일 개수
    private String statusNm; //진행상황

    private BoardPostDTO answers; // FAQ, QnA 답변 정보
    private String answerYn; //QnA 답변여부
    private String answerStatus; // 답변 상태
    
    //신규
    private String qnaStatus; //QNA 상태값
    private String bbsSeCd; // 게시판유형
    private String bbsSubSeCd; // 게시판유형 상세
    private String isPermanentDisplayBoard; // 종료일(end_dt)에 관계없이 게시글이 상시 노출되는 게시판 여부 (대상: 채용공고, 입찰공고)

    private List<FileDTO> attachments; // 게시글 첨부파일 목록
}
