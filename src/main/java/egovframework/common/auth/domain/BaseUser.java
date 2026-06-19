package egovframework.common.auth.domain;

import egovframework.common.annotation.Encrypted;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class BaseUser implements UserDetails {
    private static final long serialVersionUID = 1L;

    protected Long userOid;
    protected String userNmKorn;
    protected String userNmEng;
    protected String brdt;
    protected String userId;
    @ToString.Exclude
    protected String pswd;
    protected String userAuthrt;
    protected String userType;
    protected String userStts;
    @Encrypted
    protected String emlLcal;
    protected String emlDmn;
    protected String emlInpTyp;
    protected String mpnoPfx;
    @Encrypted
    protected String mpnoMid;
    protected String mpnoSfx;
    protected String mpno;
    protected Long instOid;
    protected String instNm;
    protected String deptCd;
    protected String deptNm;
    protected String jbpsCd;
    protected String jbpsNm;
    protected LocalDateTime lastLgnDt;
    protected Integer lgnFailCnt;
    protected String prvcUseYn;
    protected LocalDateTime prvcStartDt;
    protected LocalDateTime prvcEndDt;
    protected String prvcRegId;
    protected String cnsgnPvsnAgreYn;
    protected String psInfoPrv3ptYn;
    protected String prvcPvsnAgreYn;
    protected String trmsAgreYn;
    protected String emlRcptnAgreYn;
    protected String useYn;
    protected String regId;
    protected LocalDateTime regDt;
    protected String mdfcnId;
    protected LocalDateTime mdfcnDt;

    // 런타임에만 사용되는 로그인 세션 ID (DB 저장 안 함)
    protected transient String loginSessionId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if ("ADMIN".equals(userAuthrt)) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.pswd;
    }

    @Override
    public String getUsername() {
        return this.userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.lgnFailCnt < 5;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "Y".equals(this.useYn);
    }
}
