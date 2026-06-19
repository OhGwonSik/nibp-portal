package egovframework.common.controller;

import com.github.pagehelper.PageInfo;
import egovframework.admin.usermenuauth.domain.MenuDto;
import egovframework.admin.usermenuauth.service.UserMenuAuthService;
import egovframework.common.annotation.CheckMenuAccess;
import egovframework.common.annotation.CheckPortalBoardAccess;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.board.domain.*;
import egovframework.common.board.dto.BoardDTO;
import egovframework.common.board.dto.BoardPostDTO;
import egovframework.common.board.enums.BoardType;
import egovframework.common.board.mapper.BoardMapper;
import egovframework.common.board.service.BoardService;
import egovframework.common.code.domain.CodeResponseDTO;
import egovframework.common.code.service.CodeService;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.file.domain.FileDTO;
import egovframework.common.search.domain.IntegratedSearchRequest;
import egovframework.common.search.service.IntegratedSearchService;
import egovframework.portal.cardnews.dto.CardnewsDTO;
import egovframework.portal.cardnews.dto.CardnewsFilter;
import egovframework.portal.cardnews.service.CardnewsService;
import egovframework.portal.faq.service.FaqService;
import egovframework.portal.menu.domain.UserMenuDTO;
import egovframework.portal.menu.service.UserMenuService;
import egovframework.portal.notice.dto.NoticeDTO;
import egovframework.portal.notice.dto.NoticeFilter;
import egovframework.portal.notice.service.NoticeService;
import egovframework.portal.qna.dto.QnaDTO;
import egovframework.portal.qna.service.QnaService;
import egovframework.portal.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName : TemplateController.java
 * @Description : template 이동 controller
 *
 * @author : tspark
 * @since : 2025. 10. 24
 * @version : 1.0
 *
 *
 * << 개정이력(Modification Information) >>
 *
 *   수정일              수정자               수정내용
 *  -------------  ------------   ---------------------
 *
 *
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class TemplateController {
    private final CodeService codeService;
    private final UserMenuService userMenuService;
    private final BoardService boardService;
    private final NoticeService noticeService;
    private final FaqService faqService;
    private final QnaService qnaService;
    private final VideoService videoService;
    private final CardnewsService cardnewsService;
    private final UserMenuAuthService userMenuAuthService;
    private final IntegratedSearchService integratedSearchService;

    private final BoardMapper boardMapper;

    private static final String PORTAL_PAGE_PATH  = "portal/page/";
    private static final String ADMIN_PAGE_PATH   = "admin/page/";
    private static final String PORTAL_BOARD_PATH = "portal/board/";
    private static final String ADMIN_BOARD_PATH  = "admin/board/";

    @Value("${server.servlet.context-path}")
    private String contextPath;

    // ==========================================
    // 포털(사용자) 영역
    // ==========================================

    /* 일반 메인 */
    @GetMapping({ "/", "/main" })
    public String goMainPage() {
        return "portal/main";
    }
    
    /* 검색해서 들어온 통합검색 페이지 */
    @PostMapping("/api/common/search")
    public String doSearchPage(IntegratedSearchRequest integratedSearchRequest, 
                              HttpSession session) {
        
        // Session에 검색 조건 저장 (URL 파라미터 없이)
        session.setAttribute("searchRequest", integratedSearchRequest);
        
        return "redirect:/page/search";
    }

    /* URL로 들어온 통합검색 페이지 */
    @GetMapping("/page/search")
    public String goSearchPage(HttpSession session, Model model) {

        List<UserMenuDTO> rootMenuList = userMenuService.selectRootMenuList();
        model.addAttribute("rootMenuList", rootMenuList);
        
        // Session에서 검색 조건 가져오기
        IntegratedSearchRequest integratedSearchRequest = 
            (IntegratedSearchRequest) session.getAttribute("searchRequest");
        
        if(integratedSearchRequest != null && 
           integratedSearchRequest.getKeyword() != null && 
           !integratedSearchRequest.getKeyword().trim().isEmpty()) {
            
            // 검색 실행
            Map<String, Object> searchData = 
                integratedSearchService.getIntegratedSearchData(integratedSearchRequest);
            model.addAttribute("searchList", searchData);
            model.addAttribute("keyword", integratedSearchRequest.getKeyword());
            
            // 사용 후 Session에서 제거
//            session.removeAttribute("searchRequest");
        } else {
            // 직접 URL로 접근한 경우
            model.addAttribute("searchList", new HashMap<String, Object>());
            model.addAttribute("keyword", "");
        }
        
        return PORTAL_PAGE_PATH + "search";
    }

    /* 일반 동적 페이지 */
    @GetMapping("/page/{targetPage}")
    @CheckMenuAccess
    public String goTargetPage(@PathVariable("targetPage") String targetPage) {
        return PORTAL_PAGE_PATH + targetPage;
    }


    // 일반 동적 게시판
    @GetMapping("/board")
    @CheckPortalBoardAccess
    public String goTargetBoardPage(@ModelAttribute BoardRequestDto dto, Model model) {
        prepareBoardModel(dto, model, false, false);

        BoardDTO boardInfo = (BoardDTO) model.getAttribute("boardInfo");
        BoardFilter filter = BoardFilter.builder().page(1).size(boardInfo.getPageCnt()).build();
        model.addAttribute("boardPostList", boardService.selectBoardPostListWithFilter(dto, filter));

        return PORTAL_BOARD_PATH + getMenuPageName(dto, false);
    }

    // 일반 동적 게시판 게시글 상세 페이지
    @GetMapping("/board/post")
    @CheckPortalBoardAccess
    public String goTargetBoardPost(@ModelAttribute BoardRequestDto dto, Model model) {
        prepareBoardModel(dto, model, true, false);
        return PORTAL_BOARD_PATH + getMenuPageName(dto, false) + "_post";
    }

    // 일반 동적 게시판 게시글 작성/수정 페이지
    @GetMapping("/board/write")
    @CheckPortalBoardAccess
    public String goTargetBoardPostWrite(@ModelAttribute BoardRequestDto dto, Model model) {
        prepareBoardModel(dto, model, false, true);
        return PORTAL_BOARD_PATH + getMenuPageName(dto, false) + "_write";
    }

    // ==========================================
    // 관리자(Admin) 영역
    // ==========================================

    /* 관리자 루트 → 메인 리다이렉트 */
    @GetMapping("/admin")
    public String redirectAdminRoot() {
        return "redirect:/admin/main";
    }

    /* 관리자 로그인 페이지 */
    @GetMapping("/admin/login")
    public String goAdminLoginPage() {
        return "admin/login";
    }

    /* 관리자 아이디/비밀번호 찾기 페이지 */
    @GetMapping("/admin/find-id-pwd")
    public String goAdminFindIdPwdPage() {
        return "admin/find_id_pwd";
    }

    // 관리자 메인
    @GetMapping("/admin/main")
    public String goAdminMain(@RequestParam(value = "error", required = false) String error,
                              Model model) {
        if (error != null && !error.isEmpty()) {
            model.addAttribute("errorMessage", error);
        }

        Map<String, Object> dashboardData = boardService.getAdminDashboardModel();
        model.addAllAttributes(dashboardData);

        return "admin/dashboard";
    }

    // admin 마이페이지
    @GetMapping("/admin/mypage")
    public String goAdminMypage(@RequestParam(value = "error", required = false) String error,
                                Model model) {
        if (error != null && !error.isEmpty()) {
            model.addAttribute("errorMessage", error);
        }
        return "admin/mypage";
    }

    // 관리자 동적 페이지
    @GetMapping("/admin/page/{targetPage}")
    @CheckMenuAccess
    public String goAdminTargetPage(@PathVariable("targetPage") String targetPage, Model model) {
        // 카테고리 목록
        model.addAttribute("categoryList", codeService.getCodeListByGrpCd("CATEGORY_TYPE"));
        return ADMIN_PAGE_PATH + targetPage;
    }

    // 관리자 동적게시판
    @GetMapping("/admin/board/{targetPage}")
    @CheckMenuAccess
    public String goAdminBoardPage(@PathVariable String targetPage, @ModelAttribute BoardRequestDto dto, Model model) {        
        prepareBoardModel(dto, model, false, false);

        BoardDTO boardInfo = (BoardDTO) model.getAttribute("boardInfo");
        BoardFilter filter = BoardFilter.builder().page(1).size(boardInfo.getPageCnt()).build();
        model.addAttribute("boardPostList", boardService.selectBoardPostListWithFilter(dto, filter));

        model.addAttribute("targetBoardMenu", userMenuService.selectBoardMenuDetail(dto));
        return ADMIN_PAGE_PATH + targetPage;
    }

    // 관리자 동적게시판 게시글 상세 페이지
    @GetMapping("/admin/board/post/{targetPage}")
    @CheckMenuAccess
    public String goAdminBoardPostPage(@PathVariable("targetPage") String targetPage,
                                       @ModelAttribute BoardRequestDto dto, Model model) {
        model.addAttribute("targetBoardMenu", userMenuService.selectBoardMenuDetail(dto));
        prepareBoardModel(dto, model, true, false);

        return ADMIN_PAGE_PATH + targetPage;
    }

    // 관리자 동적게시판 게시글 작성/수정 페이지
    @GetMapping("/admin/board/write/{targetPage}")
    @CheckMenuAccess
    public String goAdminBoardWritePage(@PathVariable("targetPage") String targetPage,
                                        @ModelAttribute BoardRequestDto dto, Model model) {
        model.addAttribute("targetBoardMenu", userMenuService.selectBoardMenuDetail(dto));

        prepareBoardModel(dto, model, false, true);

        return ADMIN_PAGE_PATH + targetPage;
    }

    // 관리자 동적으로 만든 고정게시판
    @GetMapping("/admin/board")
    @CheckPortalBoardAccess
    public String goAdminTargetBoardPage(@ModelAttribute BoardRequestDto dto, Model model) {
    	
    	// 초기 진입 시 날짜 기본값 및 관리자 플래그 설정
        if (dto.getRegStartDt() == null || dto.getRegStartDt().trim().isEmpty()) {
            dto.setRegStartDt(LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (dto.getRegEndDt() == null || dto.getRegEndDt().trim().isEmpty()) {
            dto.setRegEndDt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        
        // 관리자 페이지임을 명시
        dto.setIsAdmin("Y");
        
        prepareBoardModel(dto, model, false, false);

        BoardDTO boardInfo = (BoardDTO) model.getAttribute("boardInfo");
        BoardFilter filter = BoardFilter.builder().page(1).size(boardInfo.getPageCnt()).build();
        model.addAttribute("boardPostList", boardService.selectBoardPostListWithFilter(dto, filter));

        return ADMIN_BOARD_PATH + getMenuPageName(dto, true);
    }

    // 관리자 동적으로 만든 고정게시판 게시글 상세 페이지
    @GetMapping("/admin/board/post")
    @CheckPortalBoardAccess
    public String goAdminTargetBoardPost(@ModelAttribute BoardRequestDto dto, Model model) {
    	dto.setIsAdmin("Y");
        prepareBoardModel(dto, model, true, false);
        model.addAttribute("targetBoardMenu", userMenuService.selectBoardMenuDetail(dto));
        return ADMIN_BOARD_PATH + getMenuPageName(dto, true) + "_post";
    }

    // 관리자 동적으로 만든 고정게시판 게시글 작성/수정 페이지
    @GetMapping("/admin/board/write")
    @CheckPortalBoardAccess
    public String goAdminTargetBoardPostWrite(@ModelAttribute BoardRequestDto dto, Model model) {
    	dto.setIsAdmin("Y");
        prepareBoardModel(dto, model, false, true);
        model.addAttribute("targetBoardMenu", userMenuService.selectBoardMenuDetail(dto));
        return ADMIN_BOARD_PATH + getMenuPageName(dto, true) + "_write";
    }

    // 고정) 공지사항 게시판 view
    @GetMapping("/board/notice")
    public String goNotice(Model model) {
        NoticeFilter filter = NoticeFilter.builder()
                .page(1)
                .size(15)
                .build();
        PageInfo<?> noticePostList = noticeService.selectNoticePostListWithFilter(filter);
        model.addAttribute("NoticePostList", noticePostList);

        return PORTAL_BOARD_PATH + "notice";
    }

    // 고정) 공지사항 게시판 게시글
    @GetMapping("/board/notice/post")
    public String goNoticePost(@ModelAttribute NoticePostRequestDto noticePostDto, Model model) {
        NoticeDTO noticePost = noticeService.selectNoticeById(noticePostDto.getNtcOid());
        model.addAttribute("NoticePost", noticePost);

        return PORTAL_BOARD_PATH + "notice_post";
    }

    // 26.02.12 메뉴 삭제로 비활성화
    // 고정) qna 게시판 view
//    @GetMapping("/board/qna/main")
//    public String goQnaMain(Model model) {
//        // 카테고리 목록 조회
//        model.addAttribute("QnaCategoryList", codeService.getCodeListByGrpCd("CATEGORY_TYPE"));

    //     return PORTAL_BOARD_PATH + "qna_main";
    // }


    // 26.02.12 메뉴 삭제로 비활성화
    // 고정) qna 게시판 view
    // 기능 변경으로 인한 초기 데이터 미조회
//    @GetMapping("/board/qna")
//    public String goQna(Model model) {
//        // QnaFilter filter = QnaFilter.builder()
//        //         .page(1)
//        //         .size(10)
//        //         .build();
//        // 카테고리 목록 조회
//        model.addAttribute("QnaCategoryList", codeService.getCodeListByGrpCd("CATEGORY_TYPE"));
        // PageInfo<?> qnaPostList = qnaService.selectQnaPostListWithFilter(filter);
        // model.addAttribute("QnaPostList", qnaPostList);


    // 26.02.12 메뉴 삭제로 비활성화
    // // 고정) qna 게시판 게시글
    // @GetMapping("/board/qna/post")
    // public String goQnaPost(@ModelAttribute QnaPostRequestDto qnaPostDto, Model model) {
    // 	prepareQnaModel(qnaPostDto, model, true, false);

    //     return PORTAL_BOARD_PATH + "qna_post";
    // }

    // 26.02.12 메뉴 삭제로 비활성화
    // 고정) qna 게시판 게시글
    // @GetMapping("/board/qna/write")
    // public String goQnaWrite(@ModelAttribute QnaPostRequestDto qnaPostDto, Model model) {
    // 	prepareQnaModel(qnaPostDto, model, false, true);

    //     return PORTAL_BOARD_PATH + "qna_write";
    // }

    // 26.02.12 메뉴 삭제로 비활성화
    // 고정) faq 게시판 view
    // 기능 변경으로 인한 초기 데이터 미조회
//    @GetMapping("/board/faq")
//    public String goFaq(Model model) {
//        // 카테고리 목록 조회
//        model.addAttribute("FaqCategoryList", codeService.getCodeListByGrpCd("CATEGORY_TYPE"));

    //     // FAQ 목록 조회 (초기값)
    //     // FaqFilter filter = FaqFilter.builder()
    //     //         .page(1)
    //     //         .size(10)
    //     //         .build();
    //     // PageInfo<?> faqPostList = faqService.selectFaqPostListWithFilter(filter);
    //     // model.addAttribute("FaqPostList", faqPostList);

    //     return PORTAL_BOARD_PATH + "faq";
    // }

    // 고정) 카드뉴스 게시판 view
    @GetMapping("/board/cardnews")
    public String goCardnews(Model model) {
        CardnewsFilter filter = CardnewsFilter.builder()
                .page(1)
                .size(10)
                .build();
        PageInfo<?> cardnewsPostList = cardnewsService.selectCardnewsPostListWithFilter(filter);
        model.addAttribute("CardNewsPostList", cardnewsPostList);
        return PORTAL_BOARD_PATH + "cardnews";
    }

    // 고정) 카드뉴스 게시판 게시글
    @GetMapping("/board/cardnews/post")
    public String goCardnewsPost(@ModelAttribute CardNewsPostRequestDto cardNewsPostDto, Model model) {
        CardnewsDTO cardnewsPost = cardnewsService.selectCardnewsById(cardNewsPostDto.getCardNewsOid());
        model.addAttribute("CardNewsPost", cardnewsPost);

        return PORTAL_BOARD_PATH + "cardnews_post";
    }

    // ==========================================
    // 공통 로직
    // ==========================================

    /**
     * 게시판 및 게시글 공통 데이터 조회 및 모델 바인딩
     * 
     * 고도화시 화면에서 각 동적 게시판 별로 parameter로 받아야함
		// 리스트 보여지는 곳 = false, false
		// 상세글 = true, false
		// 수정 = false, true
		
		예시)
		boardPostStatus.java
		private Boolean isPostDetail = false
		private Boolean isWrite = false
     */
    private void prepareBoardModel(BoardRequestDto dto, Model model, boolean isPostDetail, boolean isWrite) {
        // 1. 게시판 설정 정보 조회
        BoardDTO boardInfo = boardService.selectBoard(dto);
        if (boardInfo == null) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시판 조회 오류");
        model.addAttribute("boardInfo", boardInfo);

        // 2. SubBoardType에 따른 BoardType 설정
        if (boardInfo.getBbsSubSeCd() != null) {
            model.addAttribute("boardType", BoardType.fromSubBoardType(boardInfo.getBbsSubSeCd()));
        } else {
            model.addAttribute("boardType", BoardType.fromCode(boardInfo.getBbsSeCd()));
        }

        // 3. 게시글 상세 정보 (상세보기 또는 수정 모드일 때)
        if (isPostDetail || (isWrite && dto.getBbsPstOid() != null)) {
            BoardPostDTO post = boardService.selectBoardPostDetail(dto);
            if (post == null) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
            model.addAttribute("post", post);

            // 비밀글 접근 권한 검증
            if ("Y".equals(post.getPrvtPstYn()) || "N".equals(post.getOpenYn())) {
            	HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                BaseUser user = null;
                if (authentication != null && authentication.getPrincipal() instanceof BaseUser) {
                    user = (BaseUser) authentication.getPrincipal();
                }
                
                boolean isAdmin = user != null && user.getAuthorities().stream()
                        .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
                
                boolean isOwner = user != null && post.getUserOid() != null && user.getUserOid().equals(post.getUserOid());
                
                
                HttpSession session = request.getSession();
                @SuppressWarnings("unchecked")
                Map<Long, LocalDateTime> allowedPostMap = (Map<Long, LocalDateTime>) session.getAttribute("BOARD_POST_ACCESS_ALLOW");
                
                boolean isSessionValid = allowedPostMap != null && allowedPostMap.containsKey(post.getBbsPstOid()) &&
                                         allowedPostMap.get(post.getBbsPstOid()).isAfter(LocalDateTime.now());

                // 세 가지 조건 중 하나도 만족하지 못하면 접근 차단
                if (!isAdmin && !isOwner && !isSessionValid) {
                    throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "비밀글 접근 권한이 없습니다.");
                }
            }

            // 첨부파일 조회
            if (boardInfo.getFileUldCnt() != null && boardInfo.getFileUldCnt() > 0) {
                FileDTO fileDTO = new FileDTO();
                BoardType currentType = BoardType.fromCode(boardInfo.getBbsSeCd());
                Long targetPk = null;

                if (currentType == BoardType.QNA_BOARD) {
                    // answers 객체와 bbsPstOid가 모두 null이 아닐 때만 할당
                    if (post.getAnswers() != null && post.getAnswers().getBbsPstOid() != null) {
                        targetPk = post.getAnswers().getBbsPstOid();
                    }
                } else {
                    targetPk = post.getBbsPstOid();
                }

                // targetPk가 결정된 경우에만 조회 실행
                if (targetPk != null) {
                    fileDTO.setTblOid(targetPk);
                    fileDTO.setTblNm("bbs_pst");
                    model.addAttribute("fileList", boardService.selectBoardAttachList(fileDTO));
                }
            }

            // 댓글 조회
            if ("Y".equals(boardInfo.getCmntPsbltyYn())) {
                model.addAttribute("commentList", boardService.selectBoardCommentList(post));
            }

            // 이전/다음글 조회 (상세보기시에만)
            if (isPostDetail && "Y".equals(boardInfo.getPrevNextExpsrYn())) {
                post.setIsPermanentDisplayBoard(
                    BoardType.isPermanentDisplayBoard(boardInfo.getBbsSubSeCd()) ? "Y" : "N"
                );
                model.addAttribute("prevBoardPost", boardService.selectPrevBoardPost(post));
                model.addAttribute("nextBoardPost", boardService.selectNextBoardPost(post));
            }
        }

        // 4. 작성 페이지 공통 (카테고리 등)
        if (isWrite) {
            model.addAttribute("categoryList", codeService.getCodeListByGrpCd("CATEGORY_TYPE"));
        }

        // 5. 해외 언론동향 게시판일 경우 카테고리 추가
        if (boardInfo.getBbsSubSeCd() != null && boardInfo.getBbsSubSeCd().equals(BoardType.MEDIA_TRENDS_OVERSEAS_BOARD.getBbsSubSeCd())) {
            List<CodeResponseDTO> mediaCategoryList = codeService.getCodeListByGrpCd("MEDIA_OVERSEAS_CATEGORY");
            model.addAttribute("mediaCategoryList", mediaCategoryList);

            if (mediaCategoryList != null && dto.getCtgry() != null) {
                mediaCategoryList.stream()
                        .filter(c -> c.getCdVal().equals(dto.getCtgry()))
                        .findFirst()
                        .ifPresent(c -> dto.setCtgry(c.getCdNm()));
            }
        }
    }

    /**
     * 메뉴 정보 조회 공통
     */
    private String getMenuPageName(BoardRequestDto dto, boolean isAdmin) {
        Object menu = isAdmin ? userMenuAuthService.selectBoardMenuDetail(dto) : userMenuService.selectBoardMenuDetail(dto);
        if (menu == null) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "메뉴 조회 오류");

        return (menu instanceof UserMenuDTO) ? ((UserMenuDTO) menu).getMenuPage() : ((MenuDto) menu).getMenuPage();
    }
    
    /**
     * 고정 Q&A 공통 데이터 조회 및 권한 검증
     */
    private void prepareQnaModel(QnaPostRequestDto dto, Model model, boolean isPostDetail, boolean isWrite) {
        // 1. 상세 조회 또는 수정 모드일 때 데이터 조회
        if (isPostDetail || (isWrite && dto.getQnaOid() != null)) {
            QnaDTO post = qnaService.selectQnaById(dto.getQnaOid());
            if (post == null) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
            
            // 2. 비밀글 접근 권한 검증 (상세보기 시)
            if (isPostDetail && "Y".equals(post.getPrvtPstYn())) {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                BaseUser user = null;
                if (authentication != null && authentication.getPrincipal() instanceof BaseUser) {
                    user = (BaseUser) authentication.getPrincipal();
                }

                // 관리자 여부
                boolean isAdmin = user != null && user.getAuthorities().stream()
                        .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
                
                // 본인 글 여부 (로그인 사용자 기준)
                boolean isOwner = user != null && post.getUserId() != null && user.getUserId().equals(post.getUserId());

                // 세션 인증 여부 (비인증 사용자가 비밀번호 모달을 통해 인증한 경우)
                HttpSession session = request.getSession();
                @SuppressWarnings("unchecked")
                Map<Long, LocalDateTime> allowedQnaMap = (Map<Long, LocalDateTime>) session.getAttribute("QNA_POST_ACCESS_ALLOW");
                
                boolean isSessionValid = allowedQnaMap != null && allowedQnaMap.containsKey(post.getQnaOid()) &&
                                         allowedQnaMap.get(post.getQnaOid()).isAfter(LocalDateTime.now());

                // 관리자도 아니고, 주인도 아니고, 세션 인증도 안됐으면 차단
                if (!isAdmin && !isOwner && !isSessionValid) {
                    throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "비밀글 접근 권한이 없습니다.");
                }
            }
            
            model.addAttribute("QnaPost", post);
        } else if (isWrite && dto.getQnaOid() == null) {
            // 신규 작성 시 빈 객체 주입 (타임리프 에러 방지)
            model.addAttribute("QnaPost", new QnaDTO());
        }

        // 3. 공통 데이터 (카테고리 목록 등)
        model.addAttribute("QnaCategoryList", codeService.getCodeListByGrpCd("CATEGORY_TYPE"));
    }
}
