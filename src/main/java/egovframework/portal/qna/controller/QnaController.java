package egovframework.portal.qna.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;

import egovframework.common.api.ApiResponse;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.captcha.service.CaptchaService;
import egovframework.portal.qna.dto.QnaDTO;
import egovframework.portal.qna.dto.QnaFilter;
import egovframework.portal.qna.dto.QnaInsertDTO;
import egovframework.portal.qna.service.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/portal/qna")
public class QnaController {
    private final QnaService qnaService;
    private final CaptchaService captchaService;

    /**
     * QnA 게시판 게시글 목록 조회
     *
     * @param filter QnaFilter
     * @return ResponseEntity<ApiResponse<PageInfo<?>>>
     */
    @GetMapping("/list/filter")
    public ResponseEntity<ApiResponse<PageInfo<?>>> selectQnaPostListWithFilter(@ModelAttribute QnaFilter filter) {
        PageInfo<?> qnaList = qnaService.selectQnaPostListWithFilter(filter);
        return ResponseEntity.ok(ApiResponse.success(qnaList));
    }

    /**
     * Q&A 저장
     *
     * @param qnaInsertDTO       QnaInsertDTO
     * @param files              첨부파일
     * @param captchaAnswer      캡차 답변
     * @param user               사용자 정보
     * @param request            HttpServletRequest
     * @return ResponseEntity<ApiResponse<T>>
     */
    @PostMapping("/insert")
    public ResponseEntity<ApiResponse<T>> insertQna(@RequestPart("data") @Valid QnaInsertDTO qnaInsertDTO,
                                                    @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                    @RequestPart("captchaAnswer") String captchaAnswer,
                                                    @AuthenticationPrincipal BaseUser user,
                                                    HttpServletRequest request) throws IOException {

        // 캡차 검증
        if (!captchaService.validateCaptcha(request, captchaAnswer)) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.BAD_REQUEST, "자동등록방지 문자가 일치하지 않습니다."), HttpStatus.BAD_REQUEST);
        }

        // 작성자 ID 설정
        if (user != null) {
            qnaInsertDTO.setUserId(user.getUserId());
        }
        qnaService.insertQna(qnaInsertDTO, files);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);

    }
    
    /**
     * 고정 Q&A 게시글 수정
     */
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<T>> updateQna(@RequestPart("data") @Valid QnaInsertDTO qnaInsertDTO,
                                                    @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                    @RequestPart(value = "captchaAnswer", required = false) String captchaAnswer,
                                                    @AuthenticationPrincipal BaseUser user,
                                                    HttpServletRequest request) throws IOException {

        // 1. 기존 게시글 정보 조회
    	QnaDTO existingQnaPost = qnaService.selectQnaOneById(qnaInsertDTO);
        if (existingQnaPost == null) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.NOT_FOUND, "게시글이 존재하지 않습니다."), HttpStatus.NOT_FOUND);
        }

        // 2. 권한 검증 (isadmin pass)
        boolean isAdmin = user != null && user.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

        if (!isAdmin) {
            if (existingQnaPost.getUserId() != null) {
                // 작성자 ID와 로그인 유저 ID가 같은지 확인
                if (user == null || !existingQnaPost.getUserId().equals(user.getUserId())) {
                    return new ResponseEntity<>(ApiResponse.error(HttpStatus.FORBIDDEN, "수정 권한이 없습니다."), HttpStatus.FORBIDDEN);
                }
            } else {
                // 세션 체크 (board userno가 없으면 세션 체크)
                HttpSession session = request.getSession();
                @SuppressWarnings("unchecked")
                Map<Long, LocalDateTime> allowedPostMap = (Map<Long, LocalDateTime>) session.getAttribute("QNA_POST_ACCESS_ALLOW");

                if (allowedPostMap == null || !allowedPostMap.containsKey(existingQnaPost.getQnaOid()) ||
                    allowedPostMap.get(existingQnaPost.getQnaOid()).isBefore(LocalDateTime.now())) {
                    return new ResponseEntity<>(ApiResponse.error(HttpStatus.FORBIDDEN, "비밀번호 인증이 필요하거나 수정 권한이 없습니다."), HttpStatus.FORBIDDEN);
                }
            }
        }

        // 3. 캡차 검증 (필요 시)
        if (StringUtils.hasText(captchaAnswer)) {
            if (!captchaService.validateCaptcha(request, captchaAnswer)) {
                return new ResponseEntity<>(ApiResponse.error(HttpStatus.BAD_REQUEST, "자동등록방지 문자가 일치하지 않습니다."), HttpStatus.BAD_REQUEST);
            }
        }

        // 4. 수정자 정보 설정
        if (user != null) {
            qnaInsertDTO.setMdfcnId(user.getUserId());
        }

        // 5. 게시글 수정 실행
        qnaService.updateQna(qnaInsertDTO, files);

        // 6. 수정 성공 시 세션에서 해당 게시글 권한 제거 (비회원용)
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        Map<Long, LocalDateTime> allowedPostMap = (Map<Long, LocalDateTime>) session.getAttribute("QNA_POST_ACCESS_ALLOW");
        if (allowedPostMap != null && allowedPostMap.containsKey(qnaInsertDTO.getQnaOid())) {
            allowedPostMap.remove(qnaInsertDTO.getQnaOid());
            session.setAttribute("BOARD_POST_ACCESS_ALLOW", allowedPostMap);
        }

        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
    }    
    
    /**
     * QnA 상세 조회 (권한 체크 로직 수정)
     */
    @GetMapping("/post/verify-access/{qnaOid}")
    public ResponseEntity<ApiResponse<?>> verifyAccessQnaPost(@PathVariable Long qnaOid,
                                                       @AuthenticationPrincipal BaseUser user,
                                                       @RequestParam(value = "mode", defaultValue = "view", required = false) String mode, // mode 추가
                                                       HttpServletRequest request) {

        QnaDTO post = qnaService.selectQnaById(qnaOid);
        if (post == null) {
            return new ResponseEntity<>(ApiResponse.error(HttpStatus.NOT_FOUND, "게시글이 존재하지 않습니다."), HttpStatus.NOT_FOUND);
        }

        // 공개글(비밀글 아님) 처리 -> 바로 통과
        if (!"Y".equals(post.getPrvtPstYn()) && !"upd".equals(mode)) {
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
        
        if (allowedPostMap != null && allowedPostMap.containsKey(qnaOid)) {
            LocalDateTime expireTime = allowedPostMap.get(qnaOid);
            
            // 만료 시간(expireTime)> 현재 시간(now)
            if (expireTime != null && expireTime.isAfter(LocalDateTime.now())) {
                isSessionValid = true;
            } else {
                // 시간이 지났으면 맵에서 지워주기
                allowedPostMap.remove(qnaOid);
            }
        }

        if (isSessionValid) {
            return ResponseEntity.ok(ApiResponse.success(post));
        }

        // 작성자 본인 확인 (회원인 경우) -> 바로 통과
        boolean isMemberPost = (post.getUserId() != null); // 회원글 여부 판단
        if (user != null && isMemberPost && post.getUserId().equals(user.getUserId())) {
            return ResponseEntity.ok(ApiResponse.success(post));
        }

        // 권한 없음 처리
        if (isMemberPost) {
            // 회원이 작성한 비밀글
            // 관리자도 아니고 본인도 아닐경우 -> 타인(회원/비회원 불문)
            // 접근 불가(403)
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
    
    @PostMapping("/check/pasw")
    public ResponseEntity<ApiResponse<Boolean>> checkQnAPassword(@RequestBody QnaDTO qnaDTO, HttpServletRequest request){
        Boolean checkedPassword = qnaService.checkQnAPassword(qnaDTO);

        // 성공시 세션에 qnaOid 기입
        if(checkedPassword) {
            HttpSession session = request.getSession();
            @SuppressWarnings("unchecked")
            Map<Long, LocalDateTime> allowedPostMap = (Map<Long, LocalDateTime>) session.getAttribute("BOARD_POST_ACCESS_ALLOW");
            // 처음이면 없을수도있으니까 초기화
            if (allowedPostMap == null) {
                allowedPostMap = new HashMap<>();
            }
            // 현재 시간으로부터 5분 뒤를 만료 시간으로 설정
            LocalDateTime expireTime = LocalDateTime.now().plusMinutes(5);
            allowedPostMap.put(qnaDTO.getQnaOid(), expireTime);
            
            session.setAttribute("QNA_POST_ACCESS_ALLOW", allowedPostMap);
        }
        
        return ResponseEntity.ok(ApiResponse.success(checkedPassword));
    }

    /**
     * 캡차 이미지 생성
     *
     * @param request HttpServletRequest
     * @return ResponseEntity<byte[]>
     */
    @GetMapping("/captcha")
    public ResponseEntity<byte[]> getCaptcha(HttpServletRequest request) {
        byte[] imageBytes = captchaService.generateCaptchaImage(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
    
    /**
     * QNA 사용자 만족도 평가 저장
     *
     * @param request QnaDTO
     * @return ResponseEntity<ApiResponse<Integer>>
     */
    @PatchMapping("/satisfaction")
    public ResponseEntity<ApiResponse<Integer>> insertSatisfactionByQna(@RequestBody QnaDTO qnaDTO){
        int result = qnaService.insertSatisfactionByQna(qnaDTO);
        
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }    
}
