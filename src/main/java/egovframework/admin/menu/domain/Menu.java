package egovframework.admin.menu.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
public class Menu {
    private Long menuOid;
    private String menuNm;
    private String menuCd;
    private String menuPage;
    private String menuUrl;
    private Long upMenuOid;
    private Integer menuLv;
    private Integer menuSeq;
    private String bbsUseYn;
    private String menuType;
    private String prvcUseYn;
    private String expsrYn;
    private String useYn;
    private String menuAuthLv;
    private String iconNm;
    private String menuExpln;
    private String regId;
    private LocalDateTime regDt;
    private String mdfcnId;
    private LocalDateTime mdfcnDt;
}
