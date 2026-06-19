package egovframework.portal.periodical.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : Portal502FileDTO.java
 * @Description : 정기발간자료 첨부파일 DTO
 *
 * @author : j.h.kim
 * @since : 2025. 01. 13
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Portal502FileDTO {
    private Long fileOid;            // 파일번호
    private String strgFilePath;        // 파일 경로
    private String strgFileNm;        // 저장 파일명
    private String orgnlFileNm;  // 원본 파일명
    private String fileTypeNm;         // 파일 확장자
    private Long strgFileCpct;          // 파일 크기
    private Integer dwnldCnt;    // 다운로드 수
    private String fileType;        // 파일 타입 (COVER: 표지, FULL: 총권)
}
