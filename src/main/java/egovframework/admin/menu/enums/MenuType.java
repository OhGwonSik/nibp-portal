package egovframework.admin.menu.enums;

public enum MenuType {
    LARGE("대메뉴"),
    MIDDLE("중메뉴"),
    SMALL("소메뉴"),
    PAGE("페이지"),
    TAB("탭"),
    HIDDEN("숨김"),
    BOARD("게시판")
    ;

    private final String description;

    MenuType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
