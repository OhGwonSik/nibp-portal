package egovframework.common.enums;

public enum PermissionType {
    READ("읽기"),
    WRITE("쓰기"),
    DELETE("삭제"),
    EXCEL("엑셀"),
    // FILE("파일"),
    PRINT("인쇄");

    private final String name;

    PermissionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
