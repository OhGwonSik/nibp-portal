package egovframework.portal.survey.domain;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import egovframework.common.annotation.Encrypted;
import egovframework.common.annotation.Masked;
import egovframework.common.enums.MaskingType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SurveyParticipateDTO {
	@NotNull(message = "설문 번호는 필수입니다.")
    @Positive(message = "설문 번호는 양수여야 합니다.")
	private Long srvyOid;  // 설문 번호
	
    @Encrypted
    @Masked(type = MaskingType.IP_ADDRESS)
    private String ipAddr;
}
