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
public class GroupCodeResponseDTO {
    private Integer grpCdOid;
    private String grpCd; // 그룹코드
    private String grpCdNm; // 그룹코드명
    private String grpCdExpln; // 그룹코드설명
    private String useYn; // 사용여부
    private String regId; // 등록자ID
    private LocalDateTime regDt; // 등록일시
    private String mdfcnId; // 수정자ID
    private LocalDateTime mdfcnDt; // 수정일시
}
