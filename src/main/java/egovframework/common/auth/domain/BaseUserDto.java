package egovframework.common.auth.domain;

import egovframework.common.annotation.Encrypted;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class BaseUserDto {
    private Long userOid;
    private String userNmKorn;
    private String userNmEng;
    private String brdt;
    private String userId;
    private String userAuthrt;
    private String userType;
    private String userStts;
    @Encrypted
    @ToString.Exclude
    private String emlLcal;
    private String emlDmn;
    private String emlInpTyp;
    private String mpnoPfx;
    @Encrypted
    @ToString.Exclude
    private String mpnoMid;
    private String mpnoSfx;
    @ToString.Exclude
    private String mpno;
    private Long instOid;
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
    private String emlRcptnAgreYn;
    private String useYn;
    private String regId;
    private LocalDateTime regDt;
    private String mdfcnId;
    private LocalDateTime mdfcnDt;

    public static BaseUserDto fromUser(BaseUser user) {
        if (user == null) {
            return null;
        }

        return BaseUserDto.builder()
                .userOid(user.getUserOid())
                .userNmKorn(user.getUserNmKorn())
                .userNmEng(user.getUserNmEng())
                .brdt(user.getBrdt())
                .userId(user.getUserId())
                .userAuthrt(user.getUserAuthrt())
                .userType(user.getUserType())
                .userStts(user.getUserStts())
                .emlLcal(user.getEmlLcal())
                .emlDmn(user.getEmlDmn())
                .emlInpTyp(user.getEmlInpTyp())
                .mpnoPfx(user.getMpnoPfx())
                .mpnoMid(user.getMpnoMid())
                .mpnoSfx(user.getMpnoSfx())
                .mpno(user.getMpno())
                .instOid(user.getInstOid())
                .instNm(user.getInstNm())
                .deptCd(user.getDeptCd())
                .deptNm(user.getDeptNm())
                .jbpsCd(user.getJbpsCd())
                .jbpsNm(user.getJbpsNm())
                .lastLoginDt(user.getLastLgnDt())
                .lgnFailCnt(user.getLgnFailCnt())
                .prvcUseYn(user.getPrvcUseYn())
                .prvcPvsnAgreYn(user.getPrvcPvsnAgreYn())
                .trmsAgreYn(user.getTrmsAgreYn())
                .emlRcptnAgreYn(user.getEmlRcptnAgreYn())
                .useYn(user.getUseYn())
                .regId(user.getRegId())
                .regDt(user.getRegDt())
                .mdfcnId(user.getMdfcnId())
                .mdfcnDt(user.getMdfcnDt())
                .build();
    }
}
