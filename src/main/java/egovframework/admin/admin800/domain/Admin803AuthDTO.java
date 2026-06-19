package egovframework.admin.admin800.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class Admin803AuthDTO {
    private Long userMenuAuthrtOid;     //bigint(20) 사용자메뉴권한번호
    @NotNull(message = "사용자번호는 필수 입력값입니다.")
    private Long userOid;             //bigint(20) 사용자번호
    private Long menuOid;             //bigint(20) 메뉴번호
    private String inqAuthrtYn;              //char(1) 조회권한
    private String wrtAuthrtYn;             //char(1) 쓰기권한
    private String delAuthrtYn;            //char(1) 삭제권한
    private String excelAuthrtYn;             //char(1) 엑셀권한
    private String otptAuthrtYn;             //char(1) 출력권한
    private LocalDate authBgngDt;      //date 권한시작일
    private LocalDate authEndDt;        //date 권한종료일
    private String regId;           //varchar(10) 등록자ID
    private LocalDateTime regDt;        //timestamp 등록일시
    private String mdfcnId;           //varchar(10) 수정자ID
    private LocalDateTime mdfcnDt;        //timestamp 수정일시
    private String useYn;               //char(1) 사용여부

    private List<Admin803AuthDTO> authList = new ArrayList<>();
}
