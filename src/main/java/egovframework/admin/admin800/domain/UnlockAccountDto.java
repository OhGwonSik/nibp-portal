package egovframework.admin.admin800.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class UnlockAccountDto {
    @NotNull
    private Integer userOid;                   // 사용자 번호
    private String mdfcnId;                 // 수정자 ID
    private LocalDateTime mdfcnDt;              // 수정일시
}
