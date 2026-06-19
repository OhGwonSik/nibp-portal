package egovframework.admin.admin800.domain;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.*;
@Getter
@Setter
@ToString

public class Admin807DTO {

	private Long bbsOid;
	
    @NotBlank(message = "게시판 유형은 필수입니다.")
    private String bbsSeCd = "COMMON";

    @NotBlank(message = "사용여부는 필수입니다.")
    @Pattern(regexp = "Y|N", message = "사용여부는 Y 또는 N만 가능합니다.")
    private String useYn = "Y";
    
    @NotBlank(message = "통계여부는 필수입니다.")
    @Pattern(regexp = "Y|N", message = "통계여부는 Y 또는 N만 가능합니다.")
    private String statsYn;

    @NotBlank(message = "게시판명은 필수입니다.")
    @Size(max = 200, message = "게시판명은 최대 200자까지 가능합니다.")
    private String bbsNm; 

    @Pattern(regexp = "Y|N") 
    private String upendFixYn = "N";        // 상단고정
    
    @Pattern(regexp = "Y|N") 
    private String prvtYn = "N";          // 비밀글
    
    @Pattern(regexp = "Y|N") 
    private String mbrWrtYn = "N";         // 회원만 글작성
    
    @Pattern(regexp = "Y|N") 
    private String prevNextExpsrYn = "N";         // 이전글/다음글
    
    @Pattern(regexp = "Y|N") 
    private String cmntPsbltyYn = "N";         // 댓글
    
    @Pattern(regexp = "Y|N") 
    private String ctgryYn = "N";       // 분류
    
    @Pattern(regexp = "Y|N") 
    private String listCnExpsrYn = "N";      // 내용노출
    
    private String ctgryList; // 보류 - 규칙 미정

    @NotNull(message = "파일 업로드 개수는 필수입니다.")
    @Min(value = 0, message = "파일 업로드 개수는 0 이상이어야 합니다.")
    @Max(value = 10, message = "파일 업로드 개수는 최대 10개까지 가능합니다.")
    private Integer fileUldCnt = 0;

    @NotNull(message = "파일 업로드 용량은 필수입니다.")
    @Min(value = 1, message = "파일 업로드 용량은 최소 1MB 이상이어야 합니다.")
    @Max(value = 500, message = "파일 업로드 용량은 500MB 이하만 가능합니다.")
    private Integer fileUldSize = 500;

    @Pattern(regexp = "Y|N") 
    private String pstOidExpsrYn = "N";
    
    @Pattern(regexp = "Y|N") 
    private String wrtrExpsrYn = "N";
    
    @Pattern(regexp = "Y|N") 
    private String wrtDtExpsrYn = "N";
    
    @Pattern(regexp = "Y|N") 
    private String ddlnDtExpsrYn = "N";
    
    @Pattern(regexp = "Y|N") 
    private String atchFileEnYn = "N";
    
    @Pattern(regexp = "Y|N") 
    private String prgrsSttsExpsrYn = "N";
    
    @Pattern(regexp = "Y|N") 
    private String inqCntExpsrYn = "N";
    
    @Pattern(regexp = "Y|N")
    private String srchUseYn = "Y";

    @NotNull(message = "페이지당 게시글수는 필수입니다.")
    @Min(value = 1, message = "페이지당 게시글수는 1 이상이어야 합니다.")
    private Integer pageCnt = 10;
    
    @NotNull(message = "페이징수는 필수입니다.")
    @Min(value = 1, message = "페이징수는 1 이상이어야 합니다.")
    private Integer pagingCnt = 5;

    private Integer imgWdthCnt = 4;
    private Integer imgVrtcCnt = 3;

    private String bbsExpln;
    private String menuAuthLv;

    private String regId;
    private String mdfcnId;
	
}