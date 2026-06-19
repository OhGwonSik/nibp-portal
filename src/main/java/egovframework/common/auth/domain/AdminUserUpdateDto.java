package egovframework.common.auth.domain;

import egovframework.common.annotation.Encrypted;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class AdminUserUpdateDto {
    // @NotBlank
    // private String userId;
    @NotBlank
    @Size(min = 1, max = 50, message = "성명(한글)은 1자 이상 50자 이하로 입력해주세요.")
    private String userNmKorn;
    @NotBlank
    @Size(min = 1, max = 50, message = "성명(영문)은 1자 이상 50자 이하로 입력해주세요.")
    private String userNmEng;
    @NotBlank
    @Size(min = 8, max = 8, message = "생년월일은 8자로 입력해주세요.")
    @Pattern(regexp = "^[0-9]{8}$", message = "생년월일은 8자로 입력해주세요.")
    private String brdt;
    @Encrypted
    @NotBlank
    @Size(min = 1, max = 64, message = "이메일 주소는 1자 이상 64자 이하로 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9]([._-]?[a-zA-Z0-9]+)*$", message = "올바른 이메일 형식이 아닙니다.")
    private String emlLcal;
    @NotBlank
    @Size(min = 1, max = 255, message = "이메일 도메인은 1자 이상 255자 이하로 입력해주세요.")
    @Pattern(regexp = "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$", message = "맞지 않는 이메일 형식입니다.") // 도메인 정규식
    private String emlDmn;
    @NotBlank
    @Size(min = 3, max = 3, message = "휴대폰 번호는 3자로 입력해주세요.")
    @Pattern(regexp = "^(010|011|016|017|018|019)$", message = "휴대폰 번호 형식에 맞지 않습니다.")
    private String mpnoPfx;
    @Encrypted
    @NotBlank
    @Size(min = 4, max = 4, message = "휴대폰 번호는 4자로 입력해주세요.")
    @Pattern(regexp = "^\\d{4}$", message = "휴대폰 번호 형식에 맞지 않습니다.")
    private String mpnoMid;
    @NotBlank
    @Size(min = 4, max = 4, message = "휴대폰 번호는 4자로 입력해주세요.")
    @Pattern(regexp = "^\\d{4}$", message = "휴대폰 번호 형식에 맞지 않습니다.")
    private String mpnoSfx;
    @NotBlank
    @Size(min = 1, max = 100, message = "부서명은 1자 이상 100자 이하로 입력해주세요.")
    private String deptNm;
    @NotBlank
    @Size(min = 1, max = 50, message = "직급명은 1자 이상 50자 이하로 입력해주세요.")
    private String jbpsNm;
    private String emlInpTyp;
    private LocalDateTime mdfcnDt;
}
