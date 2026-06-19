package egovframework.admin.admin800.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin803AccountVO {
	private Integer userOid; //사용자번호
	private String userNmKorn; //성명(한글)
	private String userNmEng; //성명(영문)
	private String userId; //사용자ID
	private String useYn; //사용여부
	private String prvcUseYn; //개인정보취급권한
}
