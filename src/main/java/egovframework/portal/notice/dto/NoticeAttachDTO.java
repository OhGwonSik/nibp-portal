package egovframework.portal.notice.dto;

import egovframework.common.board.domain.BaseAttachDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString(callSuper = true)
public class NoticeAttachDTO extends BaseAttachDTO {
    private Long noticeAttachNo;        // 공지 첨부 id
    private Long ntcOid;              // 공지 id
    private Long strgFileCpct;              // 파일 크기
    private String regId;           // 등록자ID
    private LocalDateTime regDt;        // 등록일시
    private String mdfcnId;           // 수정자ID
    private LocalDateTime mdfcnDt;        // 수정일시
}