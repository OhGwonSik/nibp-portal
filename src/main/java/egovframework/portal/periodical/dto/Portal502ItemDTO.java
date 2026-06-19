package egovframework.portal.periodical.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : Portal502ItemDTO.java
 * @Description : 정기발간자료 아이템 DTO (각 논문/부록 항목)
 *
 * @author : j.h.kim
 * @since : 2025. 01. 13
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Portal502ItemDTO {
    private String fxtmPblsItemTtl;     // 제목
    private String aut;                 // 저자
    private String pageNum;             // 페이지
    private Long atchFileOid;           // 첨부파일번호
    private String attachFilePath;      // 첨부파일 경로
    private String attachFileName;      // 첨부파일 저장파일명
    private String attachOriginFileName;// 첨부파일 원본파일명
}
