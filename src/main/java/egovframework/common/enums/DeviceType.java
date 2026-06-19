package egovframework.common.enums;

public enum DeviceType {
    PC("PC"),
    MOBILE("MOBILE"),
    TABLET("TABLET"),
    ETC("ETC");

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
