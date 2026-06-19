package egovframework.admin.admin500.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin502QuestionDTO {

	private Long srvyQitemOid;
    private String srvyQitemType;      // MULTI, LIKERT, RANK, SHORT...
    private String srvyQitemTtl;
    private int srvyQitemSeq;
    private String displayQNo;  // 화면 표시 번호 (1, 1-1)
    private String plrlChcYn;
    private String parentSrvyQitemType;
    private Long upSrvyQitemOid;

    // 제한 설정값 (최대 N개 선택)
    private Integer srvyQitemLmt;

    // 리커트 설정값
    private Integer likertMin;
    private Integer likertMax;
    private String likertMinLbl;
    private String likertMaxLbl;

    // 통합된 하위 데이터 리스트
    // options: 객관식, 리커트(점수), 비율형
    private List<Admin502OptionDTO> options = new ArrayList<>();
    // responses: 주관식, 순위형, 이미지응답형, 기타의견
    private List<Admin502ResponseDTO> responses = new ArrayList<>();
}
