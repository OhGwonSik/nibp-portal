package egovframework.admin.admin800.domain;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupCode {
    private Integer grpCdOid;
    @NotBlank(message = "그룹코드는 필수 입력값입니다.")
    @Size(max = 50, message = "그룹코드는 50자 이하로 입력해주세요.")
    private String grpCd; // 그룹코드
    @NotBlank(message = "그룹코드명은 필수 입력값입니다.")
    @Size(max = 200, message = "그룹코드명은 200자 이하로 입력해주세요.")
    private String grpCdNm; // 그룹코드명
    private String grpCdExpln; // 그룹코드설명
    private String useYn;        // 사용여부
    private String regId;    // 등록자ID
    private LocalDateTime regDt; // 등록일시
    private String mdfcnId;    // 수정자ID
    private LocalDateTime mdfcnDt; // 수정일시

}
