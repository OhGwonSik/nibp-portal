package egovframework.admin.admin800.domain;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
/**
 * @ClassName : Admin810.java
 * @Description : 기관 정보 / target inst
 *
 * @author : balee
 * @since  : 2025. 11. 11
 * @version : 1.0
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin810 {
    private String instOid;     //BIGINT(20) 기관번호
    @NotBlank(message = "기관명은 필수 입력값입니다.")
    @Size(max = 200, message = "기관명은 200자 이하로 입력해주세요.")
    private String instNm;     //VARCHAR(200) 기관명
    @NotBlank(message = "사업자등록번호는 필수 입력값입니다.")
    @Size(max = 12, message = "사업자등록번호는 12자 이하로 입력해주세요.")
    private String bizRegNo;  //VARCHAR(12) 사업자등록번호
    @NotBlank(message = "대표자명은 필수 입력값입니다.")
    @Size(max = 100, message = "대표자명은 100자 이하로 입력해주세요.")
    private String repNm;     //VARCHAR(100) 대표자명
    @NotBlank(message = "전화번호 앞자리는 필수 입력값입니다.")
    @Size(max = 4, message = "전화번호 앞자리는 4자 이하로 입력해주세요.")
    private String telPfx;    //VARCHAR(4) 전화번호접두어
    @NotBlank(message = "전화번호 중간자리는 필수 입력값입니다.")
    @Size(max = 4, message = "전화번호 중간자리는 4자 이하로 입력해주세요.")
    private String telMid;    //VARCHAR(4) 전화번호중간번호
    @NotBlank(message = "전화번호 뒷자리는 필수 입력값입니다.")
    @Size(max = 4, message = "전화번호 뒷자리는 4자 이하로 입력해주세요.")
    private String telSfx;    //VARCHAR(4) 전화번호접미어
    @Size(max = 4, message = "팩스번호 앞자리는 4자 이하로 입력해주세요.")
    private String faxPfx;    //VARCHAR(4) 팩스번호접두어
    @Size(max = 4, message = "팩스번호 중간자리는 4자 이하로 입력해주세요.")
    private String faxMid;    //VARCHAR(4) 팩스번호중간번호
    @Size(max = 4, message = "팩스번호 뒷자리는 4자 이하로 입력해주세요.")
    private String faxSfx;    //VARCHAR(4) 팩스번호접미어
    @Size(max = 6, message = "우편번호는 6자 이하로 입력해주세요.")
    private String zipCd;     //VARCHAR(6) 우편번호
    @Size(max = 500, message = "주소는 500자 이하로 입력해주세요.")
    private String addr;      //VARCHAR(500) 주소
    @Size(max = 500, message = "상세주소는 500자 이하로 입력해주세요.")
    private String addrDtl;   //VARCHAR(500) 상세주소

    private String regId; //VARCHAR(10) 등록자ID (서버에서 자동 설정)
    private String regDt;     //TIMESTAMP 등록일시
    private String mdfcnId; //VARCHAR(10) 수정자ID
    private String mdfcnDt;     //TIMESTAMP 수정일시
    private String useYn;     //CHAR(1) 사용 여부 (Y/N)
}
