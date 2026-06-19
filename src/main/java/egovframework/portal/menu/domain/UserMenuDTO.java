package egovframework.portal.menu.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMenuDTO {
    private Long menuOid;            //메뉴번호
    private String menuNm;          //메뉴명
    private String menuCd;          //메뉴코드
    private String menuPage;        //메뉴페이지
    private String menuUrl;         //메뉴URL
    private Long upMenuOid;      //상위메뉴번호
    private Integer menuLv;      //메뉴레벨
    private Integer menuSeq;      //메뉴순서
    private String menuType;        //메뉴타입
    private String menuAuthLv;       // 권한레벨
    private String bbsUseYn;      // 게시판 사용여부
    private String useYn;           //사용여부
    private String expsrYn;          // 노출여부
    private String npagYn;     // 새창열기여부
    private String iconNm;       //아이콘클래스
    private String menuExpln;        //메뉴설명
    private String regId;       //등록자ID
    private LocalDateTime regDt;    //등록일시
    private String mdfcnId;       //수정자ID
    private LocalDateTime mdfcnDt;    //수정일시
}
