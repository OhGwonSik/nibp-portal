package egovframework.admin.admin500.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin502RespondentDTO {

	private Long srvyOid;        // 조회할 설문 번호

    private Long srvyRspdntOid;          // 실제 응답자 번호 (PK)
    private Long rowNum;          // 화면 표시용 순번 
    private String regDt;         // 참여 일시
}
