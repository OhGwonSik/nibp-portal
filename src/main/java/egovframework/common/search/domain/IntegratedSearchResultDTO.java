package egovframework.common.search.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class IntegratedSearchResultDTO {

    private String bbsSeCd;     // 게시판 구분 (POST / NOTICE)
    private Long postId;          // 게시글 PK (post_no)
    private Long noticeNo;        // 공지사항 PK (notice_no)
    private Long bbsOid;         // 게시판 번호 (동적 게시판용)
    private String menuCd;        // 메뉴 코드 (상세 이동 및 LNB 매칭용)

    private String menuNm;        // 메뉴명 (게시판 이름)
    private String menuPath;      // 메뉴 계층 경로 (Home > Depth1 > Depth2)
    private String title;         // 검색된 제목 (post_title 또는 notice_nm)
    private String content;       // 검색된 본문 요약 (태그 제거된 텍스트)
    private String wrtrNm;      // 작성자명
    
    private LocalDateTime regDate;       // 등록일자 (포맷팅된 문자열)
    private int viewCount;        // 조회수 (인기순 정렬용)
    private double score;         // 검색 정확도 점수 (관련도순 정렬용)
    
    private Integer rootMenuNo;   // 대메뉴 PK (그룹핑 키)
    private String rootMenuNm;    // 대메뉴 이름 (화면 표시용)
    private long groupTotalCnt;   // 해당 그룹 전체 건수 (PageInfo 세팅용)
    
    private String viewUrl;      // 상세페이지 이동 URL
}
