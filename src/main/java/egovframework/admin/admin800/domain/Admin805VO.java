package egovframework.admin.admin800.domain;

import egovframework.common.annotation.Encrypted;
import egovframework.common.annotation.Masked;
import egovframework.common.enums.MaskingType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin805VO {
	private Long prmsnChgLogOid;
	@Encrypted
	@Masked(type = MaskingType.USER_ID)
	private String chnrgUserId;
	@Encrypted
	@Masked(type = MaskingType.USER_ID)
	private String trgtUserId;
	private Long userMenuAuthrtOid;
	private Long menuOid;
	private String menuNm;
	private String prmsnType;
	private String oldVl;
	private String newVl;
	private String chgType;
	private String chgDt;
	private String rsn;
	private String regDt;
	@Encrypted
	@Masked(type = MaskingType.USER_ID)
	private String regId;
}
