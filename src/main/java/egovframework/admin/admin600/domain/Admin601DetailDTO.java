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
public class Admin601DetailDTO {
    // ====== ntc ======
    private Long ntcOid;		    		// 공지id
    private String ntcNm;				// 공지명
    private String ntcCn;					// 내용

    private LocalDateTime bgngDt;			// 게시 시작 일시
    private LocalDateTime endDt;			// 게시 종료 일시

    private String upendFixYn;				// 상단 고정 여부
    private LocalDateTime upendFixBgngDt;	// 상단 고정 시작 일시
    private LocalDateTime upendFixEndDt;		// 상단 고정 종료 일시
    private String openYn;					// 공개 여부
    private String useYn;					// 사용 여부
    private Integer inqCnt;				// 조회수

    private String regId;		        // 등록자 ID
    private LocalDateTime regDt;	        // 등록일시
    private String mdfcnId;		        // 수정자 ID
    private LocalDateTime mdfcnDt;	        // 수정일시

    private List<FileDTO> noticeAttachList;
    private List<FileDTO> noticeCkEditorAttachList;
}
