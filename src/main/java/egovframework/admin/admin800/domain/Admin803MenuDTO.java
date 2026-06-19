package egovframework.admin.admin800.domain;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin803MenuDTO {
    private Long menuOid;            //메뉴번호
    private String menuNm;          //메뉴명
    private String menuCd;          //메뉴코드
    private String menuUrl;         //메뉴URL
    private Long upMenuOid;      //상위메뉴번호
    private Integer menuLv;      //메뉴레벨
    private Integer menuSeq;      //메뉴순서
    private String useYn;           //사용여부
    private LocalDateTime regDt;    //등록일시
    private LocalDate authBgngDt;   //권한부여일자
    private LocalDate authEndDt; //권한만료일자

    private String inqAuthrtYn;          //조회권한
    
    private String userId;          //아이디

    @Builder.Default
    private List<Admin803MenuDTO> subMenus = new ArrayList<>();
}
