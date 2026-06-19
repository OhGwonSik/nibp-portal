package egovframework.common.util;

import egovframework.common.annotation.Encrypted;
import egovframework.common.component.AESComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * @Encrypted 어노테이션이 붙은 필드를 자동으로 암호화/복호화하고,
 * SHA-256 해시를 생성하는 유틸리티 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CryptoUtil {
    private final AESComponent aesComponent;

    /**
     * 객체의 @Encrypted 필드를 자동으로 암호화합니다.
     *
     * @param obj 암호화할 객체
     * @param <T> 객체 타입
     * @return 암호화된 객체 (동일한 인스턴스)
     */
    public <T> T encrypt(T obj) {
        if (obj == null) {
            return null;
        }

        try {
            processFields(obj, true);
        } catch (Exception e) {
            log.error("암호화 처리 중 오류 발생: {}", e.getMessage(), e);
        }

        return obj;
    }

    /**
     * 리스트 내 모든 객체의 @Encrypted 필드를 자동으로 암호화합니다.
     *
     * @param list 암호화할 객체 리스트
     * @param <T> 객체 타입
     * @return 암호화된 리스트 (동일한 인스턴스)
     */
    public <T> List<T> encryptList(List<T> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }

        list.forEach(this::encrypt);
        return list;
    }

    /**
     * 객체의 @Encrypted 필드를 자동으로 복호화합니다.
     *
     * @param obj 복호화할 객체
     * @param <T> 객체 타입
     * @return 복호화된 객체 (동일한 인스턴스)
     */
    public <T> T decrypt(T obj) {
        if (obj == null) {
            return null;
        }

        try {
            processFields(obj, false);
        } catch (Exception e) {
            log.error("복호화 처리 중 오류 발생: {}", e.getMessage(), e);
        }

        return obj;
    }

    /**
     * 리스트 내 모든 객체의 @Encrypted 필드를 자동으로 복호화합니다.
     *
     * @param list 복호화할 객체 리스트
     * @param <T> 객체 타입
     * @return 복호화된 리스트 (동일한 인스턴스)
     */
    public <T> List<T> decryptList(List<T> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }

        list.forEach(this::decrypt);
        return list;
    }

    /**
     * Reflection을 사용해 @Encrypted 필드를 찾아 암호화/복호화 처리
     *
     * @param obj 처리할 객체
     * @param isEncrypt true면 암호화, false면 복호화
     */
    private void processFields(Object obj, boolean isEncrypt) throws Exception {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            // @Encrypted 어노테이션이 있고, String 타입인 필드만 처리
            if (field.isAnnotationPresent(Encrypted.class) && field.getType() == String.class) {
                field.setAccessible(true);

                String value = (String) field.get(obj);

                // null이거나 빈 문자열은 건너뛰기
                if (value == null || value.isEmpty()) {
                    continue;
                }

                try {
                    String processedValue = isEncrypt
                        ? aesComponent.encrypt(value)
                        : aesComponent.decrypt(value);

                    field.set(obj, processedValue);

                    log.debug("필드 {}이(가) {}되었습니다.", field.getName(), isEncrypt ? "암호화" : "복호화");
                } catch (Exception e) {
                    log.error("필드 {} {}중 오류 발생: {}",
                        field.getName(),
                        isEncrypt ? "암호화 " : "복호화 ",
                        e.getMessage());
                    // 개별 필드 처리 실패해도 다른 필드는 계속 처리
                }
            }
        }
    }

    /**
     * 문자열을 SHA-256으로 해시합니다.
     * 동일한 입력에 대해 항상 동일한 해시값을 반환합니다.
     *
     * @param plainText 해시할 평문
     * @return SHA-256 해시값 (64자 16진수 문자열)
     */
    public String hash(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            String hashValue = DigestUtils.sha256Hex(plainText);
            log.debug("문자열이 SHA-256으로 해시되었습니다. (길이: {})", plainText.length());
            return hashValue;
        } catch (Exception e) {
            log.error("SHA-256 해시 생성 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 검색용 해시를 생성합니다.
     * hash() 메소드와 동일하지만, 검색 용도임을 명확히 하기 위한 별칭 메소드입니다.
     *
     * @param plainText 해시할 평문
     * @return SHA-256 해시값 (64자 16진수 문자열)
     */
    public String hashForSearch(String plainText) {
        return hash(plainText);
    }

    /**
     * Salt를 추가하여 SHA-256 해시를 생성합니다.
     * 같은 평문이라도 salt가 다르면 다른 해시값이 생성됩니다.
     *
     * @param plainText 해시할 평문
     * @param salt 솔트 값
     * @return SHA-256 해시값 (64자 16진수 문자열)
     */
    public String hashWithSalt(String plainText, String salt) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        if (salt == null) {
            salt = "";
        }

        try {
            String combined = plainText + salt;
            String hashValue = DigestUtils.sha256Hex(combined);
            log.debug("Salt가 적용된 SHA-256 해시가 생성되었습니다.");
            return hashValue;
        } catch (Exception e) {
            log.error("Salt 적용 SHA-256 해시 생성 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 랜덤 Salt를 생성합니다.
     * 비밀번호 해시 등에 사용할 수 있습니다.
     *
     * @return Base64로 인코딩된 랜덤 Salt (16바이트)
     */
    public String generateSalt() {
        return generateSalt(16);
    }

    /**
     * 지정된 길이의 랜덤 Salt를 생성합니다.
     *
     * @param length Salt의 바이트 길이 (최소 8바이트 권장)
     * @return Base64로 인코딩된 랜덤 Salt
     */
    public String generateSalt(int length) {
        if (length < 8) {
            log.warn("Salt 길이가 너무 짧습니다. 최소 8바이트를 권장합니다.");
            length = 8;
        }

        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[length];
            random.nextBytes(salt);
            String saltString = Base64.getEncoder().encodeToString(salt);
            log.debug("{}바이트 랜덤 Salt가 생성되었습니다.", length);
            return saltString;
        } catch (Exception e) {
            log.error("Salt 생성 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
}
