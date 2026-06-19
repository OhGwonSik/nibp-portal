package egovframework.common.captcha.service.impl;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.audio.AudioCaptcha;
import cn.apiclub.captcha.audio.Sample;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.noise.CurvedLineNoiseProducer;
import cn.apiclub.captcha.text.producer.NumbersAnswerProducer;
import egovframework.common.captcha.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * 캡차 서비스 구현 클래스
 */
@Slf4j
@Service
public class CaptchaServiceImpl extends EgovAbstractServiceImpl implements CaptchaService {

    private static final String CAPTCHA_SESSION_KEY = "CAPTCHA_ANSWER";
    private static final int CAPTCHA_WIDTH = 200;
    private static final int CAPTCHA_HEIGHT = 50;
    private static final int CAPTCHA_LENGTH = 5;

    /**
     * 캡차 이미지 생성
     *
     * @param request HttpServletRequest
     * @return 캡차 이미지 바이트 배열
     */
    @Override
    public byte[] generateCaptchaImage(HttpServletRequest request) {
        try {
            // 캡차 생성 (숫자 5자리)
            Captcha captcha = new Captcha.Builder(CAPTCHA_WIDTH, CAPTCHA_HEIGHT)
                    .addText(new NumbersAnswerProducer(CAPTCHA_LENGTH))
                    .addBackground(new GradiatedBackgroundProducer())
                    .addNoise(new CurvedLineNoiseProducer())
                    .build();

            // 세션에 캡차 정답 저장
            HttpSession session = request.getSession();
            session.setAttribute(CAPTCHA_SESSION_KEY, captcha.getAnswer());

            // 이미지를 byte array로 변환
            BufferedImage image = captcha.getImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Captcha generation error", e);
            throw new RuntimeException("캡차 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 캡차 검증
     *
     * @param request HttpServletRequest
     * @param userAnswer 사용자 입력 답변
     * @return 검증 결과
     */
    @Override
    public boolean validateCaptcha(HttpServletRequest request, String userAnswer) {
        try {
            HttpSession session = request.getSession();
            String sessionCaptcha = (String) session.getAttribute(CAPTCHA_SESSION_KEY);

            // 세션에 캡차가 없거나 답변이 일치하지 않으면 false
            if (sessionCaptcha == null || !sessionCaptcha.equals(userAnswer)) {
                return false;
            }

            // 검증 성공 후 세션에서 제거 (재사용 방지)
            session.removeAttribute(CAPTCHA_SESSION_KEY);
            return true;
        } catch (Exception e) {
            log.error("Captcha validation error", e);
            return false;
        }
    }

	@Override
	public byte[] generateCaptchaAudio(HttpServletRequest request) {
		try {
            HttpSession session = request.getSession();
            String answer = (String) session.getAttribute(CAPTCHA_SESSION_KEY);

            if (answer == null || answer.equals("")) {
                return new byte[0];
            }

            // AudioCaptcha 생성
            AudioCaptcha audioCaptcha = new AudioCaptcha.Builder()
                    .addAnswer(() -> answer) 
                    .addNoise()
                    .build();

            // Challenge(Sample)을 가져와서 byte[]로 변환
            Sample sample = audioCaptcha.getChallenge();
            AudioInputStream audioInputStream = sample.getAudioInputStream();
            
            // AudioSystem을 이용해 WAV 파일 형식으로 쓰기
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, baos);
            
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Captcha audio generation error", e);
            throw new RuntimeException("캡차 음성 생성 오류", e);
        }
	}
}