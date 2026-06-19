package egovframework.admin.admin800.domain;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin808DTO {
    private Long menuOid;          // bigint(20) 메뉴번호
    @NotBlank(message = "메뉴명은 필수 입력값입니다.")
    @Size(max = 100, message = "메뉴명은 100자 이하로 입력해주세요.")
    private String menuNm;        // varchar(100) 메뉴명
    @Size(max = 50, message = "메뉴페이지명은 50자 이하로 입력해주세요.")
    private String menuPage;        // varchar(50) 메뉴페이지명
    @NotBlank(message = "메뉴코드는 필수 입력값입니다.")
    @Size(max = 50, message = "메뉴코드는 50자 이하로 입력해주세요.")
    private String menuCd;        // varchar(50) 메뉴코드
    @Size(max = 500, message = "메뉴URL은 500자 이하로 입력해주세요.")
    private String menuUrl;       // varchar(500) 메뉴URL
    private Long upMenuOid;    // bigint(20) 상위메뉴번호
    @NotNull(message = "메뉴레벨은 필수 입력값입니다.")
    private Integer menuLv;    // int(11) 메뉴레벨
    @NotNull(message = "메뉴 순번은 필수 입력값입니다.")
    private Integer menuSeq;    // int(11) 메뉴순서
    private Long bbsOid;      // bigint(20) 게시판번호
    @Size(max = 10, message = "메뉴타입은 10자 이하로 입력해주세요.")
    private String menuType;      // enum('LARGE', 'MIDDLE', 'SMALL', 'PAGE', 'TAB', 'BOARD', 'HIDDEN') 메뉴타입
    @Size(max = 10, message = "메뉴권한레벨은 10자 이하로 입력해주세요.")
    private String menuAuthLv;     // varchar(10) 메뉴권한레벨
    @Size(max = 1, message = "사용여부는 1자 이하로 입력해주세요.")
    @Pattern(regexp = "^(Y|N)$", message = "사용여부는 Y 또는 N만 입력 가능합니다.")
    private String useYn;         // char(1) 사용여부
    @Size(max = 1, message = "메인페이지 2차메뉴 표기여부는 1자 이하로 입력해주세요.")
    @Pattern(regexp = "^(Y|N)$", message = "메인페이지 2차메뉴 표기여부는 Y 또는 N만 입력 가능합니다.")
    private String expsrYn;         // char(1) 메인페이지 2차메뉴 표기여부
    @Size(max = 1, message = "게시판사용여부는 1자 이하로 입력해주세요.")
    @Pattern(regexp = "^(Y|N)$", message = "게시판사용여부는 Y 또는 N만 입력 가능합니다.")
    private String bbsUseYn;    // char(1) 게시판사용여부
    @Size(max = 1, message = "새창열기여부는 1자 이하로 입력해주세요.")
    @Pattern(regexp = "^(Y|N)$", message = "새창열기여부는 Y 또는 N만 입력 가능합니다.")
    private String npagYn;    // char(1) 새창열기여부
    @Size(max = 1, message = "개인정보취급여부는 1자 이하로 입력해주세요.")
    @Pattern(regexp = "^(Y|N)$", message = "개인정보취급여부는 Y 또는 N만 입력 가능합니다.")
    private String prvcUseYn;     // char(1) 개인정보취급여부
    @Size(max = 100, message = "아이콘클래스는 100자 이하로 입력해주세요.")
    private String iconNm;     // varchar(100) 아이콘클래스
    @Size(max = 500, message = "메뉴설명은 500자 이하로 입력해주세요.")
    private String menuExpln;      // varchar(500) 메뉴설명
    @Size(max = 10, message = "등록자ID는 10자 이하로 입력해주세요.")
    private String regId;     // varchar(10) 등록자ID (서버에서 자동 설정)
    private LocalDateTime regDt;  // timestamp 등록일시
    @Size(max = 10, message = "수정자ID는 10자 이하로 입력해주세요.")
    private String mdfcnId;     // varchar(10) 수정자ID
    private LocalDateTime mdfcnDt;  // timestamp 수정일시

    @Builder.Default
    private List<Admin808DTO> subMenus = new ArrayList<>();

    public static Admin808DTO convertToDto(Admin808VO vo) {
        Admin808DTO dto = new Admin808DTO();
        dto.menuOid = vo.getMenuOid();
        dto.menuNm = vo.getMenuNm();
        dto.menuPage = vo.getMenuPage();
        dto.menuCd = vo.getMenuCd();
        dto.menuUrl = vo.getMenuUrl();
        dto.upMenuOid = vo.getUpMenuOid();
        dto.menuLv = vo.getMenuLv();
        dto.menuSeq = vo.getMenuSeq();
        dto.bbsOid = vo.getBbsOid();
        dto.menuType = vo.getMenuType();
        dto.menuAuthLv = vo.getMenuAuthLv();
        dto.useYn = vo.getUseYn();
        dto.expsrYn = vo.getExpsrYn();
        dto.bbsUseYn = vo.getBbsUseYn();
        dto.npagYn = vo.getNpagYn();
        dto.prvcUseYn = vo.getPrvcUseYn();
        dto.iconNm = vo.getIconNm();
        dto.menuExpln = vo.getMenuExpln();
        dto.regId = vo.getRegId();
        dto.regDt = vo.getRegDt();
        dto.mdfcnId = vo.getMdfcnId();
        dto.mdfcnDt = vo.getMdfcnDt();
        return dto;
    }
}