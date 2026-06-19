package egovframework.common.board.enums;

import lombok.Getter;

public enum PostStatus {
    TEMP("TEMP","초안"),
    PEND("PEND","대기"),
    COMP("COMP","완료");

    @Getter
    private final String statusCode;
    @Getter
    private final String statusName;

    PostStatus(String statusCode,String statusName) {
    	this.statusCode = statusCode;
        this.statusName = statusName;
    }
}