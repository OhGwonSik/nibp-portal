package egovframework.common.checkplus.controller;

import NiceID.Check.CPClient;
import egovframework.common.checkplus.service.CheckplusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CheckPlusController {
    private final CheckplusService checkplusService;

    @Value("${file.file-path}")
    private String filePath;

    @Value("${nice.cp.siteCode}")
    private String siteCode;

    @Value("${nice.cp.sitePassword}")
    private String sitePassword;

    @Value("${nice.cp.requestUrl}")
    private String requestUrl;

    @Value("${nice.cp.returnUrl}")
    private String returnUrl;

    @Value("${nice.cp.errorUrl}")
    private String errorUrl;

    // 테스트용 본인인증 스킵 플래그
    @Value("${nice.cp.skip:true}")
    private boolean skipCheckplus;

    /**
     * NICE 본인인증
     */
    @GetMapping("/api/common/portal/checkplus/main")
    public String checkplusMain(@RequestParam("purpose") String purpose,
                                @RequestParam(value = "userId", required = false) String userId,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                HttpSession session,
                                Model model) {
        try {
            // 테스트용 스킵 처리
            if (skipCheckplus) {
                log.info("[CHECKPLUS] SKIP MODE - purpose: {}", purpose);
                session.setAttribute("CHECKPLUS_PURPOSE", purpose);

                if ("RESET_PWD".equals(purpose)) {
                    session.setAttribute("PWD_RESET_USER_ID", userId);
                } else if ("ADMIN_RESET_PWD".equals(purpose)) {
                    session.setAttribute("ADMIN_PWD_RESET_USER_ID", userId);
                }

                // 가짜 본인인증 성공 처리
                return checkplusService.handleSkipSuccess(purpose, request, response, session, model);
            }

            // 회원가입시 본인인증 : SIGNUP
            // 비밀번호 초기화 : RESET_PWD
            // 관리자 로그인 : ADMIN_LOGIN
            // 비밀번호 변경 : CHANGE_PWD
            // 관리자 비밀번호 초기화 : ADMIN_RESET_PWD
            session.setAttribute("CHECKPLUS_PURPOSE", purpose);

            if ("RESET_PWD".equals(purpose)) {
                session.setAttribute("PWD_RESET_USER_ID", userId);
            } else if ("ADMIN_RESET_PWD".equals(purpose)) {
                session.setAttribute("ADMIN_PWD_RESET_USER_ID", userId);
            }

            CPClient niceCheck = new CPClient();

            // 요청번호 생성
            String reqSeq = niceCheck.getRequestNO(siteCode);
            session.setAttribute("REQ_SEQ", reqSeq); // 나중에 result에서 검증용

            // plainData 구성 (REQ_SEQ, RTN_URL, ERR_URL 등)
            String plainData = ""
                    + "7:REQ_SEQ"   + reqSeq.length()   + ":" + reqSeq
                    + "8:SITECODE"  + siteCode.length() + ":" + siteCode
                    + "7:RTN_URL"   + returnUrl.length()+ ":" + returnUrl
                    + "7:ERR_URL"   + errorUrl.length()+ ":" + errorUrl;

            int iReturn = niceCheck.fnEncode(siteCode, sitePassword, plainData);

            if (iReturn != 0) {
                // 오류 처리
                model.addAttribute("sMessage", "본인인증 암호화 오류: " + iReturn);
                model.addAttribute("sEncData", "");
                return "common/modal/checkplus_error";
            }

            String encData = niceCheck.getCipherData();

            model.addAttribute("sMessage", "본인인증을 진행합니다.");
            model.addAttribute("sEncData", encData);
            model.addAttribute("requestUrl", requestUrl); // 템플릿에서 action에 쓸 값

            return "common/modal/checkplus_main";
        } catch (Exception e) {
            log.error("[CHECKPLUS] 본인인증 초기화 중 오류 발생: purpose={}", purpose, e);
            model.addAttribute("errorMessage", "본인인증 처리 중 오류가 발생했습니다.");
            model.addAttribute("purpose", purpose);
            return "common/modal/checkplus_error";
        }
    }

    /**
     * NICE 성공 콜백
     */
    @GetMapping("/api/common/checkplus/success")
    public String checkplusSuccessForSignup(HttpServletRequest request,
                                            HttpSession session,
                                            HttpServletResponse response,
                                            Model model) {
        String purpose = (String) session.getAttribute("CHECKPLUS_PURPOSE");

        try {
            log.info("[CHECKPLUS] success called. uri={} query={} referer={}",
                    request.getRequestURI(),
                    request.getQueryString(),
                    request.getHeader("Referer"));
            // NICE에서 넘어온 암호화 데이터
            String encodeData = request.getParameter("EncodeData");

            // NICE 라이브러리로 복호화
            CPClient niceCheck = new CPClient();
            int result = niceCheck.fnDecode(siteCode, sitePassword, encodeData);

            if (result == 0) {
                String plainData = niceCheck.getPlainData();
                HashMap mapresult = niceCheck.fnParse(plainData);

                return checkplusService.handleSuccess(purpose, mapresult, request, response, session, model);
            } else {
                // 복호화 실패 케이스
                log.warn("[NICE][SUCCESS] fnDecode FAIL, result = {}", result);
                model.addAttribute("errorMessage", "본인인증 결과 해석 중 오류가 발생했습니다. (code=" + result + ")");
                model.addAttribute("purpose", purpose);
                return "common/modal/checkplus_error";
            }
        } catch (Exception e) {
            log.error("[CHECKPLUS] 본인인증 성공 콜백 처리 중 오류 발생: purpose={}", purpose, e);
            model.addAttribute("errorMessage", "본인인증 처리 중 오류가 발생했습니다.");
            model.addAttribute("purpose", purpose);
            return "common/modal/checkplus_error";
        }
    }

    /**
     * NICE 실패 콜백
     */
    @GetMapping("/api/common/checkplus/callback")
    public String checkplusFail(HttpServletRequest request,
                                HttpSession session,
                                Model model) {
        String reqSeq   = request.getParameter("REQ_SEQ");
        String errCode  = request.getParameter("ERR_CODE");
        String authType = request.getParameter("AUTH_TYPE");

        String message = "본인인증 중 오류가 발생했습니다.";
        if (errCode != null) {
            message += " (코드: " + errCode + ")";
        }

        model.addAttribute("requestNumber", reqSeq);
        model.addAttribute("errorCode", errCode);
        model.addAttribute("authType", authType);
        model.addAttribute("message", message);

        return "common/modal/checkplus_fail";
    }
}
