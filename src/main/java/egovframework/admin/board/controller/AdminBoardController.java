package egovframework.admin.board.controller;

import com.github.pagehelper.PageInfo;
import egovframework.common.api.ApiResponse;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.board.domain.BoardFilter;
import egovframework.common.board.domain.BoardRequestDto;
import egovframework.common.board.dto.BoardCommentDTO;
import egovframework.common.board.dto.BoardPostDTO;
import egovframework.common.board.dto.BoardPostInsertDTO;
import egovframework.common.board.dto.BoardPostSatisfactionDTO;
import egovframework.common.board.service.BoardService;
import egovframework.common.captcha.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/board")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBoardController {
    private final BoardService boardService;
    private final CaptchaService captchaService;

    /**
     * 동적 게시판 게시글 목록 조회
     *
     * @param boardRequestDto BoardRequestDto
     * @param filter BoardFilter
     * @return PageInfo<?>
     */
    @GetMapping("/list/filter")
    public PageInfo<?> getBoardList(@ModelAttribute BoardRequestDto boardRequestDto, BoardFilter filter){
    	return boardService.selectBoardPostListWithFilter(boardRequestDto, filter);
    }

    /**
     * 동적 게시판 게시글 저장 (파일 업로드, 캡차 지원)
     *
     * @param boardPostInsertDTO 게시글 정보
     * @param files 첨부파일 목록
     * @param captchaAnswer 캡차 답변
     * @param user 사용자 정보
     * @param request HttpServletRequest
     * @return ResponseEntity<ApiResponse<T>>
     */
    @PostMapping("/post/insert")
    public ResponseEntity<ApiResponse<T>> insertBoardPost(@RequestPart("data") @Valid BoardPostInsertDTO boardPostInsertDTO,
                                                          @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                          @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
                                                          @RequestPart(value = "captchaAnswer", required = false) String captchaAnswer,
                                                          @AuthenticationPrincipal BaseUser user,
                                                          HttpServletRequest request) {

        // 캡차 검증 (캡차 기능이 있는 곳은 체크 없는 곳은 패스)
        if(StringUtils.hasText(captchaAnswer)) {
            if (!captchaService.validateCaptcha(request, captchaAnswer)) {
                return new ResponseEntity<>(ApiResponse.error(HttpStatus.BAD_REQUEST, "자동등록방지 문자가 일치하지 않습니다."), HttpStatus.BAD_REQUEST);
            }
        }
        
        // 사용자 정보 설정
        if (user != null) {
            boardPostInsertDTO.setUserOid(user.getUserOid());
            boardPostInsertDTO.setRegId(user.getUserId());
            boardPostInsertDTO.setMdfcnId(user.getUserId());
        }

        boardService.insertBoardPost(boardPostInsertDTO, files, thumbnailFile);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);

    }
    
    /**
     * 동적 QNA 게시글 저장 - 어드민 (파일 업로드)
     *
     * @param boardPostInsertDTO 게시글 정보
     * @param files 첨부파일 목록
     * @param user 사용자 정보
     * @param request HttpServletRequest
     * @return ResponseEntity<ApiResponse<T>>
     */
    @PostMapping("/post/insert/qna")
    public ResponseEntity<ApiResponse<T>> insertBoardPostQNA(@RequestPart("data") @Valid BoardPostInsertDTO boardPostInsertDTO,
                                                          @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                          @AuthenticationPrincipal BaseUser user,
                                                          HttpServletRequest request) {        
        // 사용자 정보 설정
        if (user != null) {
            boardPostInsertDTO.setUserOid(user.getUserOid());
            boardPostInsertDTO.setRegId(user.getUserId());
            boardPostInsertDTO.setMdfcnId(user.getUserId());
        }

        boardService.insertBoardPostQNA(boardPostInsertDTO, files);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);

    }    
    
    /**
     * 동적 게시판 게시글 수정 (파일 업로드, 캡차 지원)
     *
     * @param boardPostInsertDTO 게시글 정보
     * @param files 첨부파일 목록
     * @param captchaAnswer 캡차 답변
     * @param user 사용자 정보
     * @param request HttpServletRequest
     * @return ResponseEntity<ApiResponse<T>>
     */
    @PostMapping("/post/update")
    public ResponseEntity<ApiResponse<T>> updateBoardPost(@RequestPart("data") @Valid BoardPostInsertDTO boardPostInsertDTO,
                                                          @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                          @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
                                                          @RequestPart(value = "captchaAnswer", required = false) String captchaAnswer,
                                                          @AuthenticationPrincipal BaseUser user,
                                                          HttpServletRequest request) {

        // 캡차 검증 (캡차 기능이 있는 곳은 체크 없는 곳은 패스)
        if(StringUtils.hasText(captchaAnswer)) {
            if (!captchaService.validateCaptcha(request, captchaAnswer)) {
                return new ResponseEntity<>(ApiResponse.error(HttpStatus.BAD_REQUEST, "자동등록방지 문자가 일치하지 않습니다."), HttpStatus.BAD_REQUEST);
            }
        }
        
        // 사용자 정보 설정
        if (user != null) {
            boardPostInsertDTO.setUserOid(user.getUserOid());
            boardPostInsertDTO.setRegId(user.getUserId());
            boardPostInsertDTO.setMdfcnId(user.getUserId());
        }

        boardService.updateBoardPost(boardPostInsertDTO, files, thumbnailFile);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);

    }    
    
    /**
     * 동적 QNA 게시판 게시글 수정 - 어드민 (파일 업로드)
     *
     * @param boardPostInsertDTO 게시글 정보
     * @param files 첨부파일 목록
     * @param user 사용자 정보
     * @param request HttpServletRequest
     * @return ResponseEntity<ApiResponse<T>>
     */
    @PostMapping("/post/update/qna")
    public ResponseEntity<ApiResponse<T>> updateBoardPostQNA(@RequestPart("data") @Valid BoardPostInsertDTO boardPostInsertDTO,
                                                          @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                          @AuthenticationPrincipal BaseUser user,
                                                          HttpServletRequest request) {
        
        // 사용자 정보 설정
        if (user != null) {
            boardPostInsertDTO.setUserOid(user.getUserOid());
            boardPostInsertDTO.setRegId(user.getUserId());
            boardPostInsertDTO.setMdfcnId(user.getUserId());
        }

        boardService.updateBoardPostQNA(boardPostInsertDTO, files);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);

    }        

    /**
     * 동적 게시판 댓글 저장
     *
     * @param boardCommentDTO BoardCommentDTO
     * @return ResponseEntity<ApiResponse<Integer>>
     */
    @PutMapping("/comment/insert")
    public ResponseEntity<ApiResponse<Integer>> insertBoardComment(@RequestBody BoardCommentDTO boardCommentDTO) {
        Integer result = boardService.insertBoardComment(boardCommentDTO);
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }
    
    /**
     * 동적 게시판 댓글 삭제
     *
     * @param boardPostDTO BoardPostDTO
     * @return ResponseEntity<ApiResponse<Integer>>
     */
    @PatchMapping("/comment/delete")
    public ResponseEntity<ApiResponse<Integer>> deleteBoardComment(@RequestBody BoardPostDTO boardPostDTO) {
        Integer result = boardService.deleteBoardComment(boardPostDTO);
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }

    /**
     * 동적 게시판 조회수 업데이트
     *
     * @param boardPostDTO 게시글 번호
     * @return ResponseEntity<ApiResponse<T>>
     */
	@PatchMapping("/view-count")
	public ResponseEntity<ApiResponse<T>> updateBoardViewCnt(@RequestBody BoardPostDTO boardPostDTO) {
        boardService.updateBoardViewCnt(boardPostDTO);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

    /**
     * 동적 게시판 상세 조회 (권한 체크)
     *
     * @param bbsPstOid 게시글 번호
     * @param user 사용자 정보
     * @param request HttpServletRequest
     * @return ResponseEntity<ApiResponse<?>>
     */
    @GetMapping("/post/verify-access/{bbsPstOid}/{menuCd}")
    public ResponseEntity<ApiResponse<?>> verifyAccessBoardPost(@PathVariable Long bbsPstOid,
    															@PathVariable String menuCd,
                                                                @AuthenticationPrincipal BaseUser user,
                                                                HttpServletRequest request) {

        BoardPostDTO post = boardService.selectBoardPostById(bbsPstOid);
        if (post == null) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.NOT_FOUND, "게시글이 존재하지 않습니다."), HttpStatus.NOT_FOUND);
        }

        // 공개글(비밀글 아님) 처리 -> 바로 통과
        if (!"Y".equals(post.getPrvtPstYn())) {
            return ResponseEntity.ok(ApiResponse.success(post));
        }

        // 비밀글('Y') 권한 체크
        // 관리자 권한 체크 -> 바로 통과
        boolean isAdmin = user != null && user.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

        if (isAdmin) {
            return ResponseEntity.ok(ApiResponse.success(post));
        }

        // 세션에 있는 접근 허용 정보 체크 - 시간체크 5분간 (비회원글 비밀번호 인증 통과자용)
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        Map<Long, LocalDateTime> allowedPostMap = (Map<Long, LocalDateTime>) session.getAttribute("BOARD_POST_ACCESS_ALLOW");

        boolean isSessionValid = false;

        if (allowedPostMap != null && allowedPostMap.containsKey(bbsPstOid)) {
            LocalDateTime expireTime = allowedPostMap.get(bbsPstOid);

            // 만료 시간(expireTime)> 현재 시간(now)
            if (expireTime != null && expireTime.isAfter(LocalDateTime.now())) {
                isSessionValid = true;
            } else {
                // 시간이 지났으면 맵에서 지워주기
                allowedPostMap.remove(bbsPstOid);
            }
        }

        if (isSessionValid) {
            return ResponseEntity.ok(ApiResponse.success(post));
        }

        // 작성자 본인 확인 (회원인 경우) -> 바로 통과
        boolean isMemberPost = (post.getUserOid() != null); // 회원글 여부 판단
        if (user != null && isMemberPost && post.getUserOid().equals(user.getUserOid())) {
            return ResponseEntity.ok(ApiResponse.success(post));
        }

        // 권한 없음 처리
        if (isMemberPost) {
            // 회원이 작성한 비밀글
            // 관리자도 아니고 본인도 아닐경우 -> 타인(회원/비회원 불문)
            // 접근 불가(403) -> 관리자라는 단어가 들어간 에러면 /main으로 가기때문에 어드민으로 변경
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.FORBIDDEN, "작성자와 어드민만 확인할 수 있는 게시글입니다."), HttpStatus.FORBIDDEN);
        } else {
            // 비회원이 작성한 비밀글
            // 관리자 아니고 세션에 접근 기록도 없음
            // 비밀번호 요구
            Map<String, String> result = new HashMap<>();
            result.put("action", "REQUIRED_PASSWORD");

            return ResponseEntity.ok(ApiResponse.success(result));
        }
    }

    /**
     * 동적 게시판 비밀번호 확인
     *
     * @param requestBody 게시글 정보
     * @param request HttpServletRequest
     * @return ResponseEntity<ApiResponse<Boolean>>
     */
    @PostMapping("/check/pasw")
    public ResponseEntity<ApiResponse<Boolean>> checkBoardPassword(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        Long bbsPstOid = Long.valueOf(requestBody.get("bbsPstOid").toString());
        String password = (String) requestBody.get("password");

        Boolean checkedPassword = boardService.checkBoardPassword(bbsPstOid, password);

        // 성공시 세션에 bbsPstOid 기입
        if (checkedPassword) {
            HttpSession session = request.getSession();
            @SuppressWarnings("unchecked")
            Map<Long, LocalDateTime> allowedPostMap = (Map<Long, LocalDateTime>) session.getAttribute("BOARD_POST_ACCESS_ALLOW");
            // 처음이면 없을수도있으니까 초기화
            if (allowedPostMap == null) {
                allowedPostMap = new HashMap<>();
            }
            // 현재 시간으로부터 5분 뒤를 만료 시간으로 설정
            LocalDateTime expireTime = LocalDateTime.now().plusMinutes(5);
            allowedPostMap.put(bbsPstOid, expireTime);

            session.setAttribute("BOARD_POST_ACCESS_ALLOW", allowedPostMap);
        }

        return ResponseEntity.ok(ApiResponse.success(checkedPassword));
    }
    
    @PatchMapping("/delete")
    public ResponseEntity<ApiResponse<Integer>> deleteBoardPostAndComment(@RequestBody List<BoardPostDTO> boardPostDTO) throws IOException {

        Integer result = boardService.deleteBoardPostAndComment(boardPostDTO);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 동적 QNA 사용자 만족도 평가 저장
     *
     * @param boardPostSatisfactionDTO BoardPostSatisfactionDTO
     * @return ResponseEntity<ApiResponse<Integer>>
     */
    @PatchMapping("/satisfaction")
    public ResponseEntity<ApiResponse<Integer>> insertSatisfactionByQna(@RequestBody @Valid BoardPostSatisfactionDTO boardPostSatisfactionDTO){
        int result = boardService.insertSatisfactionByQnaBoardPost(boardPostSatisfactionDTO);
        
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }    
}