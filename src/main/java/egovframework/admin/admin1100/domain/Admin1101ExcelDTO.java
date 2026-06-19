package egovframework.admin.admin1100.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * @ClassName : Admin1101ExcelDTO.java
 * @Description : 정기발간자료 엑셀 다운로드 DTO
 *
 * @author : j.h.kim
 * @since : 2025. 01. 12
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1101ExcelDTO {
    private Long fxtmPblsDataOid;   // 번호
    private String fxtmPblsDataTtl; // 제목
    private LocalDate pblcnDt;      // 발행일
    private String openYn;          // 공개여부
    private Integer inqCnt;        // 조회수
}
