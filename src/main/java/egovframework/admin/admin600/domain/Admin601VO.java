package egovframework.admin.admin600.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Admin601VO {
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
	private String regId;				// 등록자 ID
	private LocalDateTime regDt;			// 등록일시
	private String mdfcnId;				// 수정자 ID
	private LocalDateTime mdfcnDt;			// 수정일시

	private LocalDate regDtFrom;	        // 등록일 시작일 (등록일 시작일 검색)
	private LocalDate regDtTo;		        // 등록일 종료일 (등록일 종료일 검색)
	private Integer pageNum;		        // 페이지 번호
	private Integer pageSize;		        // 페이지 당 목록 사이즈
	private String keyword;			        // 공지 제목 검색
}
