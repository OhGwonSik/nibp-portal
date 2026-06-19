package egovframework.portal.user.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userOid;
    private String userNmKorn;
    private String userNmEng;
    private String brdt;
    private String userId;
    private String pswd;
    private String userAuthrt;
    private String userType;
    private String userStts;
    private String emlLcal;
    private String emlDmn;
    private String emlInpTyp;
    private String mpnoPfx;
    private String mpnoMid;
    private String mpnoSfx;
    private String instOid;
    private String instNm;
    private String deptCd;
    private String deptNm;
    private String jbpsCd;
    private String jbpsNm;
    private LocalDateTime lastLoginDt;
    private Integer lgnFailCnt;
    private String prvcUseYn;
    private String prvcPvsnAgreYn;
    private String trmsAgreYn;
    private String tdptyPvsnAgreYn;
    private String cnsgnPvsnAgreYn;
    private String emlRcptnAgreYn;
    private String useYn;
    private String regId;
    private LocalDateTime regDt;
    private String mdfcnId;
    private LocalDateTime mdfcnDt;
    private String currentPwd;
    private String newPswd;
    private String newPwdConfirm;
    
    // 본인인증 정보 (비밀번호 변경 시 사용)
    private String verifyName;
    private String verifyBirth;
    private String verifyMobileNo;
    
    // 탈퇴 시 사용
    private String delToken;
    private String dummyPassword;

    private int pageIndex = 1;   // 1부터 시작
    private int pageCnt  = 10;  // 한 페이지 개수

    public User convertToVO() {
        User vo = new User();

        vo.setUserOid(this.userOid);
        vo.setUserNmKorn(this.userNmKorn);
        vo.setUserNmEng(this.userNmEng);
        vo.setBrdt(this.brdt);
        vo.setUserId(this.userId);
        vo.setPswd(this.pswd);
        vo.setUserAuthrt(this.userAuthrt);
        vo.setUserType(this.userType);
        vo.setUserStts(this.userStts);
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
        vo.setLastLoginDt(this.lastLoginDt);
        vo.setLgnFailCnt(this.lgnFailCnt);
        vo.setPrvcUseYn(this.prvcUseYn);
        vo.setPrvcPvsnAgreYn(this.prvcPvsnAgreYn);
        vo.setTrmsAgreYn(this.trmsAgreYn);
        vo.setTdptyPvsnAgreYn(this.tdptyPvsnAgreYn);
        vo.setCnsgnPvsnAgreYn(this.cnsgnPvsnAgreYn);
        vo.setEmlRcptnAgreYn(this.emlRcptnAgreYn);
        vo.setUseYn(this.useYn);
        vo.setRegId(this.regId);
        vo.setRegDt(this.regDt);
        vo.setMdfcnId(this.mdfcnId);
        vo.setMdfcnDt(this.mdfcnDt);

        return vo;
    }
}
