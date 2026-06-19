package egovframework.admin.admin800.domain;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
@Getter
@Setter
@ToString

public class Admin807VO {

	private Long bbsOid;                   // 게시판번호
	private String bbsSeCd;               // 게시판유형
	private String useYn;                   // 사용여부(Y/N)
	private String statsYn;                   // 통계여부(Y/N)
	private String useYnStr;                // 사용여부텍스트(사용/미사용)
	private String bbsNm;                 // 게시판명
	private String upendFixYn;               // 상단고정여부(Y/N)
	private String prvtYn;                 // 비밀글여부(Y/N)
	private String mbrWrtYn;                // 회원만글작성여부(Y/N)
	private String prevNextExpsrYn;                // 이전글다음글노출여부(Y/N)
	private String cmntPsbltyYn;                // 댓글여부(Y/N)
	private String ctgryYn;              // 분류여부(Y/N)
	private String listCnExpsrYn;             // 목록내용노출여부(Y/N)
	private String ctgryList;            // 분류목록(TEXT)
	private Integer fileUldCnt;          // 파일업로드개수
	private Integer fileUldSize;         // 개별파일업로드용량(MB)
	private String pstOidExpsrYn;               // 게시글번호노출(Y/N)
	private String wrtrExpsrYn;            // 작성자노출(Y/N)
	private String wrtDtExpsrYn;           // 작성일노출(Y/N)
	private String ddlnDtExpsrYn;          // 마감일노출(Y/N)
	private String atchFileEnYn;            // 첨부파일유무노출(Y/N)
	private String prgrsSttsExpsrYn;            // 상태진행상황노출(Y/N)
	private String inqCntExpsrYn;            // 조회수노출(Y/N)
	private String srchUseYn;             // 검색창사용(Y/N)
	private Integer pageCnt;               // 페이지당게시글수
	private Integer pagingCnt;          // 페이징수
	private Integer imgWdthCnt;           // 페이지당이미지가로개수
	private Integer imgVrtcCnt;           // 페이지당이미지세로개수
	private String bbsExpln;               // 게시판설명(TEXT)
	private String bbsSubSeCd;            // 게시판유형 상세
	private String regId;               // 등록자ID
	private LocalDateTime regDt;            // 등록일시
	private String mdfcnId;               // 수정자ID
	private LocalDateTime mdfcnDt;            // 수정일시
	
	//게시판 위치 설정용
	private Integer positionMenuOid;
	private String positionMenuNm;
	private Integer upMenuOid;
	private String positionMenuUrl;
	private String parentMenuNm;
	private String menuUseYn;
	private String menuCd;
	
	private String srchKywd; //검색단어
	private String searchCondition; //검색키워드
	
}
