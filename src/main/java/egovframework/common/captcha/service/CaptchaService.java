package egovframework.common.captcha.service;

import javax.servlet.http.HttpServletRequest;

/**
 * 캡차 서비스 인터페이스
 */
public interface CaptchaService {

    /**
     * 캡차 이미지 생성
     *
     * @param request HttpServletRequest
     * @return 캡차 이미지 바이트 배열
     */
    byte[] generateCaptchaImage(HttpServletRequest request);
    
    /**
     * 캡차 음성 생성
     * 
     * @param request HttpServletRequest
     * @return 캡차 이미지 바이트 배열
     * */
    public byte[] generateCaptchaAudio(HttpServletRequest request);

    /**
     * 캡차 검증
     *
     * @param request HttpServletRequest
     * @param userAnswer 사용자 입력 답변
     * @return 검증 결과
     */
    boolean validateCaptcha(HttpServletRequest request, String userAnswer);
}