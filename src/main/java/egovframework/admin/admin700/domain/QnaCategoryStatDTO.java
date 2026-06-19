package egovframework.admin.admin700.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QnaCategoryStatDTO {

	//admin707
	private String gubun;       // 구분 (질의, 응답, 만족, 불만족)
    private int totalCnt;       // 계 (전체 합계)
    
    // 카테고리별 카운트
    private int researchCommon; // 연구공통
    private int humanSubject;   // 인간대상연구
    private int humanDerived;   // 인체유래물연구
    private int humanBank;      // 인체유래물은행
    private int embryoCreate;   // 배아 등의 생성 및 관리
    private int embryoResearch; // 배아 등을 이용한 연구
    private int geneTest;       // 유전자검사 및 치료
    private int committee;      // 기관위원회 운영
    private int etc;            // 기타
    
    private int unspecified;    // 미지정 (카테고리 없음)
}
