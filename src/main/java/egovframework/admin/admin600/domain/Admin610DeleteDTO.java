package egovframework.admin.admin600.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Admin610DeleteDTO {
    @NotBlank(message = "삭제할 QnA가 존재하지 않습니다.")
    private Long qnaOid;            // QnA ID
    private String userId;         // 회원 ID
    private String useYn;          // 사용 여부
    private LocalDateTime mdfcnDt;   // 수정일시
}