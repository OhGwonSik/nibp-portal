package egovframework.common.audit.enums;

public enum LoginResult {
    SUCCESS("SUCCESS"),
    FAIL("FAIL");

    private final String value;

    LoginResult(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
