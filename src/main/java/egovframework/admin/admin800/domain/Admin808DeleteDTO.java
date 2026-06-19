package egovframework.admin.admin800.domain;

import lombok.*;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin808DeleteDTO {
    private Long menuOid;          // bigint(20) 메뉴번호
    @Size(max = 1, message = "사용여부는 1자 이하로 입력해주세요.")
    private String useYn;         // char(1) 사용여부
    @Size(max = 10, message = "수정자ID는 10자 이하로 입력해주세요.")
    private String mdfcnId;     // varchar(10) 수정자ID
    private LocalDateTime mdfcnDt;  // timestamp 수정일시
}
