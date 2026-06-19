package egovframework.admin.admin800.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import egovframework.common.annotation.Encrypted;
import egovframework.common.annotation.Masked;
import egovframework.common.enums.MaskingType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin801VO {
    private Long userOid;                     // 사용자 번호
    private String userNmKorn;                 // 성명(한글)
    private String userNmEng;                 // 성명(영문)
    private String brdt;                     // 생년월일
    private String userId;                   // 사용자 ID
    private String pswd;                      // 비밀번호(해시)
    private String userAuthrt;                 // 유저권한
    private String userType;                 // 유저 타입
    private String userStts;               // 유저 상태
    @Encrypted
    @Masked(type = MaskingType.EMAIL_LOCAL_ONLY)
    private String emlLcal;                 // 이메일 로컬
    private String emlDmn;                   // 이메일 도메인
    private String emlInpTyp;                // 이메일 입력 유형
    private String mpnoPfx;                  // 휴대폰 접두어
    @Encrypted
    @Masked(type = MaskingType.PHONE_MIDDLE_ONLY)
    private String mpnoMid;                  // 휴대폰 중간번호
    private String mpnoSfx;                  // 휴대폰 접미어
    private String instOid;                     // 소속기관코드
    private String instNm;                     // 소속기관명
    private String deptCd;                    // 부서 코드
    private String deptNm;                    // 부서명
    private String jbpsCd;                    // 직위 코드
    private String jbpsNm;                    // 직위명
    private String lgnFailCnt;              // 로그인 실패 횟수
    private String prvcUseYn;                 // 개인정보취급권한 여부 (Y/N)
    private LocalDate prvcStartDt;     // 개인정보취급권한 시작일
    private LocalDate prvcEndDt;       // 개인정보취급권한 종료일
    private String prvcRegId;          // 개인정보취급권한 등록자 ID
    private String useYn;                     // 사용 여부 (Y/N)
    private String regId;                 // 등록자 ID
    private LocalDateTime regDt;              // 등록일시
    private String mdfcnId;                 // 수정자 ID
    private LocalDateTime mdfcnDt;              // 수정일시

    @Masked(type = MaskingType.PHONE_MIDDLE)
    private String phone;
    @Masked(type = MaskingType.EMAIL_LOCAL)
    private String email;
    private String regDtStr;
}
