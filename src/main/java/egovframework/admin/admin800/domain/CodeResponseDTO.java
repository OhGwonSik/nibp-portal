package egovframework.admin.admin800.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
public class CodeResponseDTO {
    private Integer cdOid;
    private Integer grpCdOid;
    private String cdVal; // 코드값
    private String cdNm; // 코드명
    private String upCdVal; // 상위코드
    private String cdExpln; // 코드설명
    private Integer cdSeq; // 정렬순서
    private String useYn; // 사용여부
    private String atrb1; // 속성1
    private String atrb2; // 속성2
    private String atrb3; // 속성3
    private String atrb4; // 속성4
    private String atrb5; // 속성5
    private String regId; // 등록자ID
    private LocalDateTime regDt; // 등록일시
    private String mdfcnId; // 수정자ID
    private LocalDateTime mdfcnDt; // 수정일시
}
