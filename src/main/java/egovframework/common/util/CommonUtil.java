package egovframework.common.util;

public class CommonUtil {
    // 인스턴스화 방지
    private CommonUtil() {}

    public static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
