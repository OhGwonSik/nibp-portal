package egovframework.admin.admin500.domain;

import egovframework.common.annotation.Encrypted;
import egovframework.common.annotation.Masked;
import egovframework.common.enums.MaskingType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin502RawDataDTO {
	
    private Long srvyRspdntOid; // 응답자 번호 (rspd_no)

    private String srvyRspdntNm; // 응답자 성명 (rspd_nm) - 비회원은 '게스트' 로 치환됨
    
    private Long srvyQitemOid; // 문항 번호 (qst_no)
    
    private String answerVal; // 답변 내용 (answer_val) - 객관식/주관식/순위형 등 통합된 값
}
