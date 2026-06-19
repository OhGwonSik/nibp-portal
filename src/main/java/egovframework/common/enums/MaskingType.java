package egovframework.common.enums;

/**
 * 마스킹 타입을 정의하는 Enum
 */
public enum MaskingType {
    /**
     * 한글 이름 마스킹
     * 예: 홍길동 -> 홍*동
     */
    NAME_KR,

    /**
     * 영어 이름 마스킹
     * 예: John Doe -> J*** D**
     */
    NAME_EN,

    /**
     * 생년월일 마스킹 (8자리)
     * 예: 19900101 -> 1990****
     */
    BIRTH_DATE,

    /**
     * 이메일 로컬파트 마스킹
     * 예: abc123@example.com -> a*****@example.com
     */
    EMAIL_LOCAL,
    /**
     * 이메일 로컬파트 마스킹(단독)
     * 예: abc123 -> a*****
     */
    EMAIL_LOCAL_ONLY,

    /**
     * 전화번호 전체 마스킹 (하이픈 없음)
     * 예: 01012345678 -> 010****5678
     */
    PHONE_FULL,

    /**
     * 전화번호 중간부분 마스킹 (하이픈 포함)
     * 예: 010-1234-5678 -> 010-****-5678
     */
    PHONE_MIDDLE,

    /**
     * 전화번호 중간부분 마스킹(단독)
     */
    PHONE_MIDDLE_ONLY,

    /**
     * 소속 마스킹
     * 예: 기술연구소 -> 기술***
     */
    ORGANIZATION,

    /**
     * 부서 마스킹
     * 예: 개발팀 -> 개*
     */
    DEPARTMENT,

    /**
     * 직위 마스킹
     * 예: 수석연구원 -> 수석***
     */
    POSITION,

    /**
     * 사용자 ID 마스킹
     * 예: user12345 -> use*****5
     */
    USER_ID,

    /**
     * IP 주소 마스킹
     * 예: 192.168.0.1 -> 192.168.*.*
     */
    IP_ADDRESS,

    /**
     * 전체 마스킹 (기타)
     * 예: any text -> *******
     */
    ALL
}
