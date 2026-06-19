package egovframework.common.enums;

import lombok.Getter;

@Getter
public enum AuthLevel {
    ADMIN("관리자"),
    COMMON("일반");

    private final String name;

    AuthLevel(String name) {
        this.name = name;
    }
}
