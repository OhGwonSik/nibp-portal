package egovframework.common.search.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class IntegratedSearchRequest {
	@NotBlank(message = "검색어를 입력해주세요.")
    @Size(min = 2, message = "검색어는 2글자 이상이어야 합니다.")
    private String keyword;

    private int pageNum = 1;      // 현재 페이지 번호
    private int pageCnt = 3;     // 페이지당 데이터 수 (초기값 3)

    private Integer rootMenuNo;   // 특정 대메뉴 더보기 시 사용 (null이면 전체검색)
    
    private String ctgry = "ALL"; // UI 구분용 (기존 유지)
    private String sort = "accuracy"; // 정확도순(score) 또는 최신순(reg_date)
}
