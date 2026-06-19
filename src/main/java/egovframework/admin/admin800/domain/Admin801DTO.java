package egovframework.admin.admin800.domain;

import egovframework.common.annotation.Encrypted;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Admin801DTO {
    // 수정 시에만 필요한 값 (신규면 null 가능)
    private Long userOid;                     // 사용자 번호

    @NotBlank(message = "성명(한글)은 필수 입력값입니다.")
    @Size(max = 50, message = "성명(한글)은 50자 이내로 입력해주세요.")
    private String userNmKorn;                 // 성명(한글)

    @NotBlank(message = "성명(영문)은 필수 입력값입니다.")
    @Size(max = 50, message = "성명(영문)은 50자 이내로 입력해주세요.")
    private String userNmEng;                 // 성명(영문)

    @NotBlank(message = "생년월일은 필수 입력값입니다.")
    @Pattern(regexp = "\\d{8}", message = "생년월일은 YYYYMMDD 형식으로 입력해주세요.")
    private String brdt;                     // 생년월일 (YYYYMMDD 형식, 예: 19990101)

    @NotBlank(message = "아이디는 필수 입력값입니다.")
    @Size(max = 10, message = "아이디는 10자 이내로 입력해주세요.")
    private String userId;                   // 사용자 ID
    private String pswd;                      // 비밀번호(해시)

    @NotBlank(message = "이메일 아이디는 필수 입력값입니다.")
    @Size(max = 64, message = "이메일 아이디는 64자 이내로 입력해주세요.")
    private String emlLcal;                 // 이메일 로컬

    @NotBlank(message = "이메일 도메인은 필수 입력값입니다.")
    @Size(max = 255, message = "이메일 도메인은 255자 이내로 입력해주세요.")
    @Encrypted
    private String emlDmn;                   // 이메일 도메인

    private String emlInpTyp;                // 이메일 입력 유형

    @NotBlank(message = "휴대폰 앞자리는 필수 입력값입니다.")
    @Pattern(regexp = "01[016-9]", message = "휴대폰 앞자리는 010/011/016/017/018/019 형식이어야 합니다.")
    private String mpnoPfx;                  // 휴대폰 접두어

    @NotBlank(message = "휴대폰 중간자리는 필수 입력값입니다.")
    @Pattern(regexp = "\\d{3,4}", message = "휴대폰 중간자리는 3~4자리 숫자여야 합니다.")
    @Encrypted
    private String mpnoMid;                  // 휴대폰 중간번호

    @NotBlank(message = "휴대폰 끝자리는 필수 입력값입니다.")
    @Pattern(regexp = "\\d{4}", message = "휴대폰 끝자리는 4자리 숫자여야 합니다.")
    private String mpnoSfx;                  // 휴대폰 접미어

    private String instOid;                     // 소속기관코드
    private String instNm;                     // 소속기관명
    private String deptCd;                    // 부서 코드

    @NotBlank(message = "부서명은 필수 입력값입니다.")
    @Size(max = 100, message = "부서는 100자 이내로 입력해주세요.")
    private String deptNm;                    // 부서명
    
    private String jbpsCd;                    // 직위 코드

    @NotBlank(message = "직위명은 필수 입력값입니다.")
    @Size(max = 50, message = "직위는 50자 이내로 입력해주세요.")
    private String jbpsNm;                    // 직위명

    private String login_fail_cnt;            // 로그인 실패 횟수

    @NotBlank(message = "개인정보취급권한 여부는 필수 선택값입니다.")
    @Pattern(regexp = "Y|N", message = "개인정보취급권한 여부는 Y 또는 N 이어야 합니다.")
    private String prvcUseYn;                 // 개인정보취급권한 여부 (Y/N)

    @NotBlank(message = "상태는 필수 선택값입니다.")
    @Pattern(regexp = "Y|N", message = "상태는 Y 또는 N 이어야 합니다.")
    private String useYn;                     // 사용 여부 (Y/N)

    private String regId;                 // 등록자 ID
    private LocalDateTime regDt;              // 등록일시
    private String mdfcnId;                 // 수정자 ID
    private LocalDateTime mdfcnDt;              // 수정일시

    /**
     * DTO를 VO로 변환
     * @return Admin801VO
     */
    public Admin801VO convertToVO() {
        Admin801VO vo = new Admin801VO();
        vo.setUserOid(this.userOid);
        vo.setUserNmKorn(this.userNmKorn);
        vo.setUserNmEng(this.userNmEng);
        vo.setBrdt(this.brdt);
        vo.setUserId(this.userId);
        vo.setPswd(this.pswd);
        vo.setEmlLcal(this.emlLcal);
        vo.setEmlDmn(this.emlDmn);
        vo.setEmlInpTyp(this.emlInpTyp);
        vo.setMpnoPfx(this.mpnoPfx);
        vo.setMpnoMid(this.mpnoMid);
        vo.setMpnoSfx(this.mpnoSfx);
        vo.setInstOid(this.instOid);
        vo.setInstNm(this.instNm);
        vo.setDeptCd(this.deptCd);
        vo.setDeptNm(this.deptNm);
        vo.setJbpsCd(this.jbpsCd);
        vo.setJbpsNm(this.jbpsNm);
        vo.setUseYn(this.useYn);
        vo.setPrvcUseYn(this.prvcUseYn);
        vo.setRegId(this.regId);
        vo.setRegDt(this.regDt);
        vo.setMdfcnId(this.mdfcnId);
        vo.setMdfcnDt(this.mdfcnDt);
        return vo;
    }
}
