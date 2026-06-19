package egovframework.common.board.enums;

import lombok.Getter;

import java.util.Set;

public enum BoardType {
    // 일반 게시판
    COMMON_BOARD("COMMON", "일반게시판", null),

    // 사용자 게시판
    USER_BOARD("USER", "사용자게시판", null),

    // FAQ 게시판
    FAQ_BOARD("FAQ", "FAQ", null),

    // QNA 게시판
    QNA_BOARD("QNA", "질문과답변", null),

    // 포토갤러리 게시판
    PHOTO_GALLERY_BOARD("PHOTO", "포토갤러리게시판", null),

    // 썸네일 게시판
    THUMBNAIL_BOARD("THUMBNAIL", "썸네일게시판", null),

    // 동적 게시판
    DYNAMIC_BOARD("DYNAMIC", "동적게시판", null),

    RECRUITMENT_BOARD("DYNAMIC", "채용공고", "RECRUITMENT"),
    GUIDELINE_BOARD("DYNAMIC", "가이드라인", "GUIDELINE"),
    BUSINESS_REPORT_BOARD("DYNAMIC", "사업결과보고서", "BUSINESS_REPORT"),
    STATISTICS_BOARD("DYNAMIC", "관련통계", "STATISTICS"),
    DISCLOSURE_BOARD("DYNAMIC", "추가공개", "DISCLOSURE"),
    BIOETHICS_QNA_BOARD("DYNAMIC", "생명윤리법 Q&A", "BIOETHICS_QNA"),
    BID_ANNOUNCEMENT_BOARD("DYNAMIC", "입찰공고", "BID_ANNOUNCEMENT"),
    DIRECTOR_ACTIVITIES_BOARD("DYNAMIC", "기관장 동향", "DIRECTOR_ACTIVITIES"),
    INSTITUTE_ALBUM_BOARD("DYNAMIC", "정책원 앨범", "INSTITUTE_ALBUM"),
    PRESS_RELEASE_BOARD("DYNAMIC", "보도자료", "PRESS_RELEASE"),
    PROJECT_REAL_NAME_BOARD("DYNAMIC", "사업실명제", "PROJECT_REAL_NAME"),
    NOTICE_BOARD("DYNAMIC", "공지사항", "NOTICE"),
    RESEARCH_REPORT_BOARD("DYNAMIC", "연구보고서", "RESEARCH_REPORT"),
    TRIP_REPORT_BOARD("DYNAMIC", "회의출장보고서", "TRIP_REPORT"),
    EVENT_PROCEEDINGS_BOARD("DYNAMIC", "행사자료집", "EVENT_PROCEEDINGS"),
    MEDIA_TRENDS_DOMESTIC_BOARD("DYNAMIC", "국내 언론동향", "MEDIA_TRENDS_DOMESTIC"),
    MEDIA_TRENDS_OVERSEAS_BOARD("DYNAMIC", "해외 언론동향", "MEDIA_TRENDS_OVERSEAS"),
    MEDIA_BRIEF_BOARD("DYNAMIC", "언론동향 브리프", "MEDIA_BRIEF"),
    BIOETHICS_HQ_BOARD("DYNAMIC", "생명윤리사업 자료실", "BIOETHICS_HQ"),
    ANTI_CORRUPTION("DYNAMIC", "반부패·청렴 활동", "ANTI_CORRUPTION"),
    JOURNAL_REGULATIONS("DYNAMIC", "학술지 관련 규정", "JOURNAL_REGULATIONS"),
    JOURNAL_FORMS("DYNAMIC", "학술지 관련 양식", "JOURNAL_FORMS");

    @Getter
    private final String boardTypeCd;

    @Getter
    private final String bbsSeCd;

    @Getter
    private final String bbsSubSeCd; // 게시판유형 상세

    BoardType(String boardTypeCd, String bbsSeCd, String bbsSubSeCd) {
        this.boardTypeCd = boardTypeCd;
        this.bbsSeCd = bbsSeCd;
        this.bbsSubSeCd = bbsSubSeCd;
    }

    /**
     * subBoardType으로 BoardType enum을 찾습니다.
     */
    public static BoardType fromSubBoardType(String subBoardType) {
        if (subBoardType == null) {
            return null;
        }
        for (BoardType type : BoardType.values()) {
            if (subBoardType.equals(type.getBbsSubSeCd())) {
                return type;
            }
        }
        return null;
    }

    /**
     * boardTypeCd로 BoardType enum을 찾습니다.
     */
    public static BoardType fromCode(String boardTypeCd) {
        if (boardTypeCd == null) {
            return null;
        }
        for (BoardType type : BoardType.values()) {
            if (type.getBoardTypeCd().equals(boardTypeCd)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 특별 안내문구를 보여줄 게시판인지 확인
     */
    public boolean shouldShowRecruitmentNotice() {
        return this == RECRUITMENT_BOARD;
    }

    /**
     * 경영공시 바로가기 버튼을 보여줄 게시판인지 확인
     */
    public boolean shouldShowDisclosureLink() {
        return this == DISCLOSURE_BOARD;
    }

    /**
     * 상단 카테고리 탭을 보여줄 게시판인지 확인
     */
    public boolean shouldShowCategoryTab() {
        return this == MEDIA_TRENDS_OVERSEAS_BOARD;
    }

    /**
     * 공지 체크박스를 보여줄 게시판인지 확인
     */
    public boolean isNoticeTypeBoard() {
        return Set.of(
                RECRUITMENT_BOARD,          // 채용공고
                BID_ANNOUNCEMENT_BOARD,     // 입찰공고
                DIRECTOR_ACTIVITIES_BOARD,  // 기관장 동향
                INSTITUTE_ALBUM_BOARD,      // 정책원 앨범
                PROJECT_REAL_NAME_BOARD     // 사업실명제
        ).contains(this);
    }

    /**
     * 동적 게시판인지 확인
     */
    public boolean isDynamicBoard() {
        return Set.of(
                RECRUITMENT_BOARD,
                GUIDELINE_BOARD,
                BUSINESS_REPORT_BOARD,
                STATISTICS_BOARD,
                DISCLOSURE_BOARD,
                BIOETHICS_QNA_BOARD,
                BID_ANNOUNCEMENT_BOARD,
                DIRECTOR_ACTIVITIES_BOARD,
                INSTITUTE_ALBUM_BOARD,
                PRESS_RELEASE_BOARD,
                PROJECT_REAL_NAME_BOARD,
                NOTICE_BOARD,
                RESEARCH_REPORT_BOARD,
                TRIP_REPORT_BOARD,
                EVENT_PROCEEDINGS_BOARD,
                MEDIA_TRENDS_DOMESTIC_BOARD,
                MEDIA_TRENDS_OVERSEAS_BOARD,
                MEDIA_BRIEF_BOARD,
                BIOETHICS_HQ_BOARD,
                ANTI_CORRUPTION,
                JOURNAL_REGULATIONS,    
                JOURNAL_FORMS
        ).contains(this);
    }

    /**
     * 종료일(end_dt)에 관계없이 게시글이 상시 노출되는 게시판 여부 (대상: 채용공고, 입찰공고)
     */
    public static boolean isPermanentDisplayBoard(String code) {
        return RECRUITMENT_BOARD.bbsSubSeCd.equals(code)
            || BID_ANNOUNCEMENT_BOARD.bbsSubSeCd.equals(code);
    }
}
