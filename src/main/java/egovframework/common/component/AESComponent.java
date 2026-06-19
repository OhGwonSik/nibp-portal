package egovframework.common.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class AESComponent {
    // AES 모드 설정 (128, 192, 256 중 선택, 기본값: 256)
    @Value("${aes.mode:128}")
    private int mode;

    // 환경변수에서 암호화 키를 읽어옵니다.
    // AES-128: 16바이트, AES-192: 24바이트, AES-256: 32바이트 (Base64 인코딩된 값)
    @Value("${aes.secret.key}")
    private String secretKeyString;

    // 128비트 (16바이트) IV - Base64 인코딩된 값
    @Value("${aes.secret.iv}")
    private String ivString;

    // IV 사용 여부 (true: CBC 모드, false: ECB 모드, 기본값: true)
    // ECB 모드는 보안상 취약하므로 가능한 CBC 모드 사용 권장
    @Value("${aes.use.iv:true}")
    private boolean useIv;

    private SecretKeySpec secretKey;
    private IvParameterSpec ivSpec;
    private String cipherTransformation;

    @PostConstruct
    public void init() {
        try {
            // 모드 검증
            if (mode != 128 && mode != 192 && mode != 256) {
                throw new IllegalArgumentException("AES 모드는 128, 192, 256 중 하나여야 합니다. 현재 값: " + mode);
            }

            byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);

            // 모드에 따른 키 길이 검증
            int expectedKeyLength = mode / 8; // 비트를 바이트로 변환
            if (keyBytes.length != expectedKeyLength) {
                throw new IllegalArgumentException(
                    String.format("AES-%d 모드는 %d바이트 키가 필요합니다. 현재 키 길이: %d바이트",
                        mode, expectedKeyLength, keyBytes.length)
                );
            }

            this.secretKey = new SecretKeySpec(keyBytes, "AES");

            // IV 사용 여부에 따라 cipher transformation 설정
            if (useIv) {
                byte[] ivBytes = Base64.getDecoder().decode(ivString);

                // IV는 항상 16바이트 (AES 블록 크기)
                if (ivBytes.length != 16) {
                    throw new IllegalArgumentException("IV는 16바이트여야 합니다. 현재 길이: " + ivBytes.length + "바이트");
                }

                this.ivSpec = new IvParameterSpec(ivBytes);
                this.cipherTransformation = "AES/CBC/PKCS5Padding";
                log.info("AESComponent initialized successfully with AES-{} CBC mode (IV enabled)", mode);
            } else {
                this.ivSpec = null;
                this.cipherTransformation = "AES/ECB/PKCS5Padding";
            }

        } catch (Exception e) {
            log.error("Failed to initialize AESComponent: {}", e.getMessage(), e);
            throw new IllegalStateException("AESComponent 초기화 실패. 환경변수를 확인하세요.", e);
        }
    }

    /**
     * 평문을 AES로 암호화합니다.
     * 암호화된 데이터는 Base64로 인코딩되어 반환됩니다.
     *
     * @param plainText 암호화할 평문
     * @return Base64 인코딩된 암호화 문자열
     * @throws Exception 암호화 중 오류 발생 시
     */
    public String encrypt(String plainText) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            Cipher cipher = Cipher.getInstance(cipherTransformation);
            if (useIv) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            }
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("AES encryption failed: {}", e.getMessage(), e);
            throw new Exception("AES 암호화 실패", e);
        }
    }

    /**
     * Base64 인코딩된 암호화 문자열을 AES로 복호화합니다.
     *
     * @param encryptedText Base64 인코딩된 암호화 문자열
     * @return 복호화된 평문
     * @throws Exception 복호화 중 오류 발생 시
     */
    public String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        try {
            Cipher cipher = Cipher.getInstance(cipherTransformation);
            if (useIv) {
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decodedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES decryption failed: {}", e.getMessage(), e);
            throw new Exception("AES 복호화 실패", e);
        }
    }

    public String getSecretKey(){
        return this.secretKeyString;
    }

    // IV 생성
    // private IvParameterSpec generateIv() {
    //     byte[] iv = new byte[16];
    //     new SecureRandom().nextBytes(iv);
    //     return new IvParameterSpec(iv);
    // }
}
