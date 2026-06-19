package egovframework.common.util;

import egovframework.common.annotation.Masked;
import egovframework.common.enums.MaskingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @Masked 어노테이션이 붙은 필드를 자동으로 마스킹하는 유틸리티 클래스
 */
@Slf4j
@Component
public class MaskingUtil {

    private static final String MASK_CHAR = "*";

    /**
     * 객체의 @Masked 필드를 자동으로 마스킹합니다.
     *
     * @param obj 마스킹할 객체
     * @param <T> 객체 타입
     * @return 마스킹된 객체 (동일한 인스턴스)
     */
    public <T> T mask(T obj) {
        if (obj == null) {
            return null;
        }

        try {
            processFields(obj);
        } catch (Exception e) {
            log.error("마스킹 처리 중 오류 발생: {}", e.getMessage(), e);
        }

        return obj;
    }

    /**
     * 리스트 내 모든 객체의 @Masked 필드를 자동으로 마스킹합니다.
     *
     * @param list 마스킹할 객체 리스트
     * @param <T> 객체 타입
     * @return 마스킹된 리스트 (동일한 인스턴스)
     */
    public <T> List<T> maskList(List<T> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }

        list.forEach(this::mask);
        return list;
    }

    /**
     * Reflection을 사용해 @Masked 필드를 찾아 마스킹 처리
     *
     * @param obj 처리할 객체
     */
    private void processFields(Object obj) throws Exception {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            // @Masked 어노테이션이 있고, String 타입인 필드만 처리
            if (field.isAnnotationPresent(Masked.class) && field.getType() == String.class) {
                field.setAccessible(true);

                String value = (String) field.get(obj);

                // null이거나 빈 문자열은 건너뛰기
                if (value == null || value.isEmpty()) {
                    continue;
                }

                try {
                    Masked annotation = field.getAnnotation(Masked.class);
                    MaskingType type = annotation.type();
                    String maskedValue = applyMasking(value, type);

                    field.set(obj, maskedValue);

                    log.debug("필드 {}이(가) {} 타입으로 마스킹되었습니다.", field.getName(), type);
                } catch (Exception e) {
                    log.error("필드 {} 마스킹 중 오류 발생: {}", field.getName(), e.getMessage());
                    // 개별 필드 처리 실패해도 다른 필드는 계속 처리
                }
            }
        }
    }

    /**
     * 마스킹 타입에 따라 마스킹 적용
     *
     * @param value 원본 값
     * @param type 마스킹 타입
     * @return 마스킹된 값
     */
    private String applyMasking(String value, MaskingType type) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        switch (type) {
            case NAME_KR:
                return maskNameKr(value);
            case NAME_EN:
                return maskNameEn(value);
            case BIRTH_DATE:
                return maskBirthDate(value);
            case EMAIL_LOCAL:
                return maskEmailLocal(value);
            case EMAIL_LOCAL_ONLY:
                return maskEmailLocalOnly(value);
            case PHONE_FULL:
                return maskPhoneFull(value);
            case PHONE_MIDDLE:
                return maskPhoneMiddle(value);
            case PHONE_MIDDLE_ONLY:
                return maskPhoneMiddleOnly(value);
            case ORGANIZATION:
                return maskOrganization(value);
            case DEPARTMENT:
                return maskDepartment(value);
            case POSITION:
                return maskPosition(value);
            case USER_ID:
                return maskUserId(value);
            case IP_ADDRESS:
                return maskIpAddress(value);
            case ALL:
                return maskAll(value);
            default:
                return value;
        }
    }

    /**
     * 한글 이름 마스킹
     * 예: 홍길동 -> 홍*동, 김철수 -> 김*수
     */
    private String maskNameKr(String name) {
        if (name.length() < 2) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + MASK_CHAR;
        }
        // 3글자 이상: 첫글자와 마지막글자 유지, 중간 마스킹
        return name.charAt(0) + MASK_CHAR.repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    /**
     * 영어 이름 마스킹
     * 예: John Doe -> J*** D**
     */
    private String maskNameEn(String name) {
        String[] parts = name.split(" ");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            String part = parts[i];
            if (part.length() > 0) {
                result.append(part.charAt(0));
                if (part.length() > 1) {
                    result.append(MASK_CHAR.repeat(part.length() - 1));
                }
            }
        }

        return result.toString();
    }

    /**
     * 생년월일 마스킹 (8자리)
     * 예: 19900101 -> 1990****
     */
    private String maskBirthDate(String birthDate) {
        if (birthDate.length() != 8) {
            return birthDate;
        }
        return birthDate.substring(0, 4) + MASK_CHAR.repeat(4);
    }

    /**
     * 이메일 로컬파트 마스킹
     * 예: abc123@example.com -> a*****@example.com
     */
    private String maskEmailLocal(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (localPart.length() == 1) {
            return localPart + domain;
        }

        return localPart.charAt(0) + MASK_CHAR.repeat(localPart.length() - 1) + domain;
    }

    /**
     * 전화번호 전체 마스킹 (하이픈 없음)
     * 예: 01012345678 -> 010****5678
     */
    private String maskPhoneFull(String phone) {
        // 숫자만 추출
        String numbers = phone.replaceAll("[^0-9]", "");

        if (numbers.length() < 8) {
            return phone;
        }

        // 앞 3자리, 뒤 4자리 유지
        String prefix = numbers.substring(0, 3);
        String suffix = numbers.substring(numbers.length() - 4);
        int maskLength = numbers.length() - 7;

        return prefix + MASK_CHAR.repeat(maskLength) + suffix;
    }

    /**
     * 전화번호 중간부분 마스킹 (하이픈 포함)
     * 예: 010-1234-5678 -> 010-****-5678
     */
    private String maskPhoneMiddle(String phone) {
        String[] parts = phone.split("-");

        if (parts.length != 3) {
            // 하이픈이 없으면 PHONE_FULL 방식으로 처리
            return maskPhoneFull(phone);
        }

        return parts[0] + "-" + MASK_CHAR.repeat(parts[1].length()) + "-" + parts[2];
    }

    /**
     * 소속 마스킹
     * 예: 기술연구소 -> 기술***
     */
    private String maskOrganization(String org) {
        if (org.length() <= 2) {
            return MASK_CHAR.repeat(org.length());
        }
        return org.substring(0, 2) + MASK_CHAR.repeat(org.length() - 2);
    }

    /**
     * 부서 마스킹
     * 예: 개발팀 -> 개*, 기술개발팀 -> 기****
     */
    private String maskDepartment(String dept) {
        if (dept.length() <= 1) {
            return MASK_CHAR;
        }
        return dept.charAt(0) + MASK_CHAR.repeat(dept.length() - 1);
    }

    /**
     * 직위 마스킹
     * 예: 수석연구원 -> 수석***
     */
    private String maskPosition(String position) {
        if (position.length() <= 2) {
            return MASK_CHAR.repeat(position.length());
        }
        return position.substring(0, 2) + MASK_CHAR.repeat(position.length() - 2);
    }

    /**
     * 사용자 ID 마스킹
     * 예: user12345 -> use*****5
     */
    private String maskUserId(String userId) {
        if (userId.length() <= 4) {
            return MASK_CHAR.repeat(userId.length());
        }
        String prefix = userId.substring(0, 3);
        String suffix = userId.substring(userId.length() - 1);
        int maskLength = userId.length() - 4;

        return prefix + MASK_CHAR.repeat(maskLength) + suffix;
    }

    /**
     * IP 주소 마스킹
     * IPv4: 192.168.0.1 -> 192.168.*.*
     * IPv6: 2001:db8:85a3::1 -> 2001:db8:85a3:0:*:*:*:*
     */
    private String maskIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ip);

            if (inetAddress instanceof Inet4Address) {
                // IPv4 마스킹 로직
                String[] parts = ip.split("\\.");
                if (parts.length != 4) {
                    return ip; // 유효하지 않은 형식은 마스킹하지 않음
                }
                return parts[0] + "." + parts[1] + ".*.*";

            } else if (inetAddress instanceof Inet6Address) {
                // IPv6 마스킹 로직: 앞 4블록 표시, 나머지 '*' 처리
                byte[] addrBytes = inetAddress.getAddress();
                StringBuilder sb = new StringBuilder();

                // 처음 4블록 (64비트)을 16진수 문자열로 변환
                for (int i = 0; i < 4; i++) {
                    sb.append(String.format("%x", ((addrBytes[i*2] & 0xff) << 8) | (addrBytes[i*2+1] & 0xff)));
                    if (i < 3) {
                        sb.append(":");
                    }
                }

                // 나머지 4블록을 '*'로 마스킹
                sb.append(":*:*:*:*");
                return sb.toString();
            }
        } catch (UnknownHostException e) {
            log.warn("Invalid IP address format for masking: {}", ip, e);
            return ip; // 마스킹 실패 시 원본 IP 반환
        }
        return ip;
    }

    /**
     * 전체 마스킹
     * 예: any text -> *******
     */
    private String maskAll(String value) {
        return MASK_CHAR.repeat(value.length());
    }

    /**
     * 휴대폰 중간자리만 마스킹 (public 메서드)
     * 중간자리 부분만 전달받아 마스킹 처리합니다.
     * 예: "1234" -> "****"
     *
     * @param middle 휴대폰 중간자리
     * @return 마스킹된 중간자리
     */
    public String maskPhoneMiddleOnly(String middle) {
        if (middle == null || middle.isEmpty()) {
            return middle;
        }
        return MASK_CHAR.repeat(middle.length());
    }

    /**
     * 이메일 로컬부분만 마스킹 (public 메서드)
     * 이메일의 로컬부분(@앞)만 전달받아 마스킹 처리합니다.
     * 예: "abc123" -> "a*****"
     *
     * @param local 이메일 로컬부분
     * @return 마스킹된 로컬부분
     */
    public String maskEmailLocalOnly(String local) {
        if (local == null || local.isEmpty()) {
            return local;
        }

        if (local.length() == 1) {
            return local;
        }

        return local.charAt(0) + MASK_CHAR.repeat(local.length() - 1);
    }
}
