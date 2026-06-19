package egovframework.admin.admin600.domain;

import egovframework.common.file.domain.FileDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class Admin610VO {
    private Long qnaOid;            // QnA ID
    private Long upQnaOid;         // 부모 QnA ID

    private String userId;         // 회원 ID (비회원 NULL)
    private String wrtNm;       // 작성자명
    private String pswd;            // 비회원 비밀번호
    private String qnaTtl;          // 제목
    private String qnaCn;        // 내용
    private String ctgry;       // 분류
    private String ctgryNm;     // 분류 명

    private String ansYn;       // 답변 완료 여부
    private String prvtPstYn;       // 비공개글 여부
    private String useYn;          // 사용 여부

    private Integer inqCnt;       // 조회수

    private LocalDateTime regDt;   // 등록일시
    private LocalDateTime mdfcnDt;   // 수정일시

    private List<Admin610ItemVO> answers;

    private List<FileDTO> qnaQuestionFiles;     // 질문 파일 목록
}
