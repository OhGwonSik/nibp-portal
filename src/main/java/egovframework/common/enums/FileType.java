package egovframework.common.enums;

import lombok.Getter;

public enum FileType {
    ATTACHMENT("ATTACHMENT", "첨부파일"),
    INLINE("INLINE", "CKEditor 이미지");

    @Getter
    private final String fileType;

    FileType(String fileType, String strgFileNm) {
        this.fileType = fileType;
    }
}