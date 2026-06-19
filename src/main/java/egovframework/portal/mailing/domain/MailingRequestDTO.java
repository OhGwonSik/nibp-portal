package egovframework.portal.mailing.domain;

import java.time.LocalDateTime;

import egovframework.common.annotation.Encrypted;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MailingRequestDTO {
	private String userNm;          // 성명
    private String mpnoFull;        // 전화번호 전체 (010-0000-0000)
    private String mpnoPfx;         // 전화번호 앞자리
    @Encrypted
    private String mpnoMid;         // 전화번호 중간자리
    private String mpnoSfx;         // 전화번호 뒷자리
    @Encrypted
    private String emlLcal;        // 이메일 로컬 (아이디)
    private String emlDmn;          // 이메일 도메인
    private String instNm;           // 소속기관명
    private String jbpsNm;          // 직위명
    private String prvcPvsnAgreYn;   // 개인정보수집동의여부 (Y/N)
    private LocalDateTime expireDt; // 만료일시
    private String secretKey;       // 암호화키
    private String certCd;        // 인증코드

    private Long emlCertOid;
    private String emlCertSttsCd;
    
    private String oldEmlLocal;     
    private String oldEmlDmn;
    
    @Encrypted
    private String newEmlLocal;     // 변경할 신규 이메일 아이디
    private String newEmlDmn;       // 변경할 신규 이메일 도메인
    private String newUserNm;
    private String delToken;        // 삭제처리 문구
    private String mode;            // 작업 구분 (CHANGE: 변경, CANCEL: 해지)
    private String subStatCd;       // 구독 상태 (SUB: 구독, UNSUB: 해지)
    
    /**
     * 하이픈이 포함된 전체 전화번호를 pfx, mid, sfx로 분리하여 세팅
     */
    public void splitMpno() {
        if (this.mpnoFull != null && this.mpnoFull.contains("-")) {
            String[] parts = this.mpnoFull.split("-");
            if (parts.length == 3) {
                this.mpnoPfx = parts[0];
                this.mpnoMid = parts[1];
                this.mpnoSfx = parts[2];
            }
        }
    }
}
