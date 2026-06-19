package egovframework.admin.admin800.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Admin803AccountDTO {
	private Integer userOid; 	 //사용자번호
	private String prvcUseYn; 	 //개인정보취급권한
	private String mdfcnId;    // 수정자ID
	private LocalDateTime mdfcnDt; // 수정일시
}
