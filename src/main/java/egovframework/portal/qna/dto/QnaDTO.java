package egovframework.portal.qna.dto;

import egovframework.common.file.domain.FileDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class QnaDTO {
    private Long qnaOid;                             // QnA ID
    private Long upQnaOid;                          // 부모 QnA ID
    private String userId;                          // 회원 ID (비회원 NULL)
    private String wrtrNm;                        // 작성자명
    private String ctgry;                        // 카테고리
    private String pswd;                             // 비회원 비밀번호
    private String qnaTtl;                           // 제목
    private String ntcCn;                         // 내용
    private String ntcCnTxt;                         // 내용
    private String ansYn;                        // 답변 완료 여부
    private String prvtPstYn;                        // 비밀글 여부
    private String useYn;                           // 사용 여부
    private Integer inqCnt;                        // 조회수
    private LocalDateTime regDt;                    // 등록일시
    private LocalDateTime mdfcnDt;                    // 수정일시

    private List<QnaDTO> answers;                   // 답변
    private List<FileDTO> attachments;              // 첨부 파일
    
    
    // qna 만족도 평가용
    private String tblNm;
    private Long tblOid;
    private String stts;
    private String chc;
    private String updId;
}