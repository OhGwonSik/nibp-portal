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
public class Admin610ItemVO {
    private Long qnaOid;            // QnA ID
    private Long upQnaOid;         // 부모 QnA ID

    private String userId;         // 회원 ID (비회원 NULL)
    private String wrtNm;       // 작성자명
    private String qnaTtl;          // 제목
    private String qnaCn;        // 내용
    private String useYn;          // 사용 여부

    private LocalDateTime regDt;   // 등록일시
    private LocalDateTime mdfcnDt;   // 수정일시
    
    // 첨부파일 목록
    private List<FileDTO> files;
}
