package egovframework.common.captcha.controller;

import egovframework.common.captcha.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 캡차 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/captcha")
public class CaptchaController {
    private final CaptchaService captchaService;

    /**
     * 캡차 이미지 생성
     *
     * @param request HttpServletRequest
     * @return ResponseEntity<byte[]>
     */
    @GetMapping("/image")
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
     * 캡차 오디오 생성
     *
     * @param request HttpServletRequest
     * @return ResponseEntity<byte[]>
     */
    @GetMapping("/audio")
    public ResponseEntity<byte[]> getCaptchaAudio(HttpServletRequest request) {
        byte[] audioBytes = captchaService.generateCaptchaAudio(request);

        HttpHeaders headers = new HttpHeaders();
        // WAV 오디오 타입 명시
        headers.setContentType(MediaType.valueOf("audio/wav")); 
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);
    }    
}