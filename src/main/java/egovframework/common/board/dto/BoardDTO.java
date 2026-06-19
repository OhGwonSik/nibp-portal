package egovframework.common.board.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
    private Long bbsOid; // 게시판번호
    private String bbsSeCd; // 게시판유형
    private String useYn; // 사용여부
    private String bbsNm; // 게시판명
    private String upendFixYn; // 댓글여부
    private String prvtYn; // 비밀글여부
    private String mbrWrtYn; // 회원만글작성여부
    private String prevNextExpsrYn; // 이전글다음글노출여부
    private String cmntPsbltyYn; // 별점여부
    private String ctgryYn; // 분류여부
    private String listCnExpsrYn; // 목록에내용노출여부
    private String ctgryList; // 분류목록
    private Integer fileUldCnt; // 파일업로드개수
    private Integer fileUldSize; // 개별업로드용량MB
    private String pstOidExpsrYn; // 게시글번호노출
    private String wrtrExpsrYn; // 작성자노출
    private String wrtDtExpsrYn; // 작성일노출
    private String ddlnDtExpsrYn; // 마감일노출
    private String atchFileEnYn; // 첨부파일유무노출
    private String prgrsSttsExpsrYn; // 상태진행상황노출
    private String inqCntExpsrYn; // 조회수노출
    private String srchUseYn; // 게시글검색창사용
    private Integer pageCnt; // 페이지당게시글수
    private Integer pagingCnt; // 페이징수
    private Integer imgWdthCnt; // 페이지당이미지가로개수
    private Integer imgVrtcCnt; // 페이지당이미지세로개수
    private String bbsExpln; // 게시판설명
    private String bbsSubSeCd; // 게시판유형 상세
    private String regId; // 등록자ID
    private LocalDateTime regDt; // 등록일시
    private String mdfcnId; // 수정자ID
    private LocalDateTime mdfcnDt; // 수정일시

    private String menuCd;
}
