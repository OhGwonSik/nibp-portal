package egovframework.common.board.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class BoardRequestDto {
    private Long bbsOid;
    private Long bbsPstOid;
    private String menuPage;
    private String menuCd;
    private String menuAuthLv;
    private String bbsSubSeCd;

    private String keyword;
    private String searchType;
    private String ctgry;
    private String regStartDt;
    private String regEndDt;
    private String openYn;
    private String prvtPstYn;
    private String isAdmin; // 어드민 검색용
    private String delYn;
    
    private String pageId;
    private String reason;
    private LocalDate bgngDt;

    private String isPermanentDisplayBoard; // 종료일(end_dt)에 관계없이 게시글이 상시 노출되는 게시판 여부 (대상: 채용공고, 입찰공고)
}
