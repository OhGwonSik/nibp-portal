package egovframework.admin.admin500.domain;

import egovframework.common.annotation.Masked;
import egovframework.common.enums.MaskingType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin501VO {

    private Long srvyOid;              // 설문번호 (PK)
    private String srvyTtl;         // 설문제목
    private String srvyBgngDt;       // 설문시작일 (date → String 처리)
    private String srvyEndDt;         // 설문종료일
    private String srvyTrgt;           // 설문대상 (MEMBER / ALL)
    private String srvyTgtNm;
    private String srvyStts;
    private String srvyType;          // 설문 유형(일반 / 만족도)
    private String srvyTypeNm;

    private String nmClctYn;         // 성명수집여부
    private String gndrClctYn;     // 성별수집여부
    private String instClctYn;        // 소속기관수집여부
    private String mpnoClctYn;       // 휴대폰번호수집여부
    private String emlClctYn;        // 이메일수집여부
    private String addrClctYn;       // 주소수집여부

    private String srvyCn;          // 설문안내
    private String atchFileUseYn;         // 첨부사용여부

    // likert 설정 (srvy에도 존재)
    private Integer likertMin;          // 최소값
    private Integer likertMax;          // 최대값
    private String likertMinLbl;        // 최소값라벨
    private String likertMaxLbl;        // 최대값라벨

    @Masked(type = MaskingType.USER_ID)
    private String regId;           // 등록자ID
    private String regDt;               // 등록일시
    private String mdfcnId;           // 수정자ID
    private String mdfcnDt;               // 수정일시
}