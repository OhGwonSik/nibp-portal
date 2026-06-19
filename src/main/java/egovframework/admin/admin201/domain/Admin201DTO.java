package egovframework.admin.admin201.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Admin201DTO {

    private Long pblDataOpnnOid;          // 의견 번호 (PK)
    private String pblDataOpnnTtl;            // 제목
    private String pblDataOpnnCn;          // 내용 (HTML)
    private String pblDataOpnnCnTxt;      // 내용 텍스트 (태그 제거)
    private String regId;        // 등록자 ID
    private LocalDateTime regDt;     // 등록일시
    private String mdfcnId;        // 수정자 ID
    private LocalDateTime mdfcnDt;     // 수정일시

}
