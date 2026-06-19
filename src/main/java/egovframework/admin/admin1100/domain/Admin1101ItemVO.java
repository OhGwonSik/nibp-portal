package egovframework.admin.admin1100.domain;

import egovframework.common.file.domain.FileDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @ClassName : Admin1101ItemVO.java
 * @Description : 정기발간자료 아이템 VO (각 논문/부록 항목)
 *
 * @author : j.h.kim
 * @since : 2025. 01. 12
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1101ItemVO {
    private Long fxtmPblsItemOid;       // 아이템번호
    private Long fxtmPblsSectOid;       // 섹션번호
    private String fxtmPblsItemTtl;     // 제목
    private String aut;                 // 저자
    private String pageNum;             // 페이지
    private Long atchFileOid;           // 첨부파일번호
    private Integer sortSeq;            // 정렬순서
    private String regId;           // 등록자ID
    private LocalDateTime regDt;        // 등록일시
    private String mdfcnId;           // 수정자ID
    private LocalDateTime mdfcnDt;        // 수정일시
    
    // 파일 정보
    private FileDTO attachments;         // 첨부파일 정보
}