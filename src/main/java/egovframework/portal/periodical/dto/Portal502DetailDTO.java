package egovframework.portal.periodical.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

/**
 * @ClassName : Portal502DetailDTO.java
 * @Description : 정기발간자료 상세 DTO
 *
 * @author : j.h.kim
 * @since : 2025. 01. 13
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Portal502DetailDTO {
    private Long fxtmPblsDataOid;       // 정기발간자료 번호
    private String fxtmPblsDataTtl;     // 총권 제목
    private String aut;                 // 저자
    private String pageNum;             // 페이지 수
    private LocalDate pblcnDt;          // 발행일
    private Integer inqCnt;            // 조회수

    // 파일 관련 정보
    private Long cvrImg1Oid;            // 표지 이미지 파일번호
    private String coverFilePath;       // 표지 이미지 파일경로
    private String coverFileName;       // 표지 이미지 저장파일명
    private String coverOriginFileName; // 표지 이미지 원본파일명
    
    // 첨부파일 목록
    private List<Portal502FileDTO> files;
    
    // 섹션 목록 (특집논문, 논문, 부록)
    private List<Portal502SectionDTO> sections;
    
    // 이전글/다음글
    private Portal502NavDTO prevPost;
    private Portal502NavDTO nextPost;
}
