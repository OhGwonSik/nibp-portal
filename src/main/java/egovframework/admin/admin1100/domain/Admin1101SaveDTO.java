package egovframework.admin.admin1100.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * @ClassName : Admin1101SaveDTO.java
 * @Description : 정기발간자료 저장/수정 DTO
 *
 * @author : j.h.kim
 * @since : 2025. 01. 12
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1101SaveDTO {
    private Long fxtmPblsDataOid;       // 정기발간자료 번호 (수정시에만 사용)

    @NotBlank(message = "총권 제목은 필수입니다.")
    private String fxtmPblsDataTtl;     // 총권 제목

    private String aut;                 // 저자
    private String pageNum;             // 페이지 수

    @NotNull(message = "발행일은 필수입니다.")
    private LocalDate pblcnDt;          // 발행일

    private Long cvrImg1Oid;            // 표지 이미지1 파일번호
    private Long cvrImg2Oid;            // 총권 파일번호

    @NotBlank(message = "공개여부는 필수입니다.")
    private String openYn;              // 공개여부(Y/N)

    // 섹션 정보
    private List<SectionSaveDTO> sections;

    @Getter
    @Setter
    @ToString
    public static class SectionSaveDTO {
        private Long fxtmPblsSectOid;   // 섹션번호 (수정시에만 사용)
        private String fxtmPblsSectType; // 섹션타입(SPECIAL, PAPER, APPENDIX)
        private String fxtmPblsSectTtl; // 섹션제목
        private Integer sortSeq;        // 정렬순서
        private List<ItemSaveDTO> items; // 아이템 목록
    }

    @Getter
    @Setter
    @ToString
    public static class ItemSaveDTO {
        private Long fxtmPblsItemOid;   // 아이템번호 (수정시에만 사용)
        private String fxtmPblsItemTtl; // 제목
        private String aut;             // 저자
        private String pageNum;         // 페이지
        private Long atchFileOid;       // 첨부파일번호
        private Integer sortSeq;        // 정렬순서
    }
}
