package egovframework.common.board.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BoardCommentDTO {

    private Long bbsCmntOid; // 댓글번호
    private Long bbsPstOid; // 게시글번호
    private String menuCd;
    private Long userOid; // 사용자번호
    private String bbsCmntCn; // 댓글내용
    private String wrtrNm; // 작성자명
    private String wrtrPswd; // 작성자비밀번호
    private Long upCmntOid; // 원댓글번호
    private Integer cmntLv; // 댓글레벨
    private String prvtCmntYn; // 비밀댓글여부
    private String delYn; // 삭제여부
    private LocalDateTime delDt; // 삭제일시
    private String regId; // 등록자ID
    private LocalDateTime regDt; // 등록일시
    private String mdfcnId; // 수정자ID
    private LocalDateTime mdfcnDt; // 수정일시
}
