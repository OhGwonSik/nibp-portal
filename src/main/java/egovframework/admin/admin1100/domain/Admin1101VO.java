package egovframework.admin.admin1100.domain;

import egovframework.common.file.domain.FileDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @ClassName : Admin1101VO.java
 * @Description : 정기발간자료 메인 VO
 *
 * @author : j.h.kim
 * @since : 2025. 01. 12
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1101VO {
    private Long fxtmPblsDataOid;       // 정기발간자료 번호
    private String fxtmPblsDataTtl;     // 총권 제목
    private String aut;                 // 저자
    private String pageNum;             // 페이지 수
    private LocalDate pblcnDt;          // 발행일
    private Long cvrImg1Oid;            // 표지 이미지1 파일번호
    private Long cvrImg2Oid;            // 총권 파일번호
    private String openYn;              // 공개여부(Y/N)
    private String useYn;               // 사용여부(Y/N)
    private Integer inqCnt;            // 조회수
    private Integer sortSeq;            // 정렬순서
    private String regId;           // 등록자ID
    private LocalDateTime regDt;        // 등록일시
    private String mdfcnId;           // 수정자ID
    private LocalDateTime mdfcnDt;        // 수정일시
    
    // 파일 정보
    private FileDTO coverFile;          // 표지 이미지 파일 정보
    private FileDTO fullFile;           // 총권 파일 정보
}
