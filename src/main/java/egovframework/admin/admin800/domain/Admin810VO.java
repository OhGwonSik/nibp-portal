package egovframework.admin.admin800.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin810VO {
    private String instOid;     //기관번호
    private String instNm;     //기관명
    private String bizRegNo;  //사업자등록번호
    private String repNm;     //대표자명
    private String telPfx;    //전화번호접두어
    private String telMid;    //전화번호중간번호
    private String telSfx;    //전화번호접미어
    private String faxPfx;    //팩스번호접두어
    private String faxMid;    //팩스번호중간번호
    private String faxSfx;    //팩스번호접미어
    private String zipCd;     //우편번호
    private String addr;      //주소
    private String addrDtl;   //상세주소
    private String regId; //등록자ID
    private String regDt;     //등록일시
    private String mdfcnId; //수정자ID
    private String mdfcnDt;     //수정일시
    private String useYn;     //사용 여부 (Y/N)
}