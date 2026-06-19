package egovframework.admin.admin800.domain;

import egovframework.common.annotation.Encrypted;
import egovframework.common.annotation.Masked;
import egovframework.common.enums.MaskingType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Admin801ResponseDto {
    private Integer userOid;          // 사용자 번호
    private String userId;           // 사용자 ID
    private String userNmKorn;         // 성명(한글)
    private String userNmEng;         // 성명(영문)
    @Encrypted
    @Masked(type = MaskingType.EMAIL_LOCAL_ONLY)
    private String emlLcal;         // 이메일 로컬
    private String emlDmn;           // 이메일 도메인
    private String mpnoPfx;
    @Encrypted
    @Masked(type = MaskingType.PHONE_MIDDLE_ONLY)
    private String mpnoMid;
    private String mpnoSfx;
    // private String mpno;             // 휴대폰 번호
    private String instNm;            // 소속기관명
    private LocalDateTime regDt;     // 등록일시
    private String useYn;            // 사용 여부 (Y/N)
    private String prvcUseYn;        // 개인정보취급권한 여부 (Y/N)
    private String prvcRegId; // 개인정보취급권한 등록자 ID
    private LocalDate prvcStartDt; // 개인정보취급권한 시작일시
    private LocalDate prvcEndDt; // 개인정보취급권한 종료일시
}
