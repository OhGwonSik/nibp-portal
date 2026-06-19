package egovframework.common.file.domain;

/**
 * 구분 값에 따라 저장 경로의 1차 디렉토리를 나누기 위한 enum.
 */
public enum FileUploadCategory {
    COMMON("common"),
    PORTAL("portal"),
    ADMIN("admin"),
    TEMP("temp");

    private final String defaultDirectory;

    FileUploadCategory(String defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
    }

    public String getDefaultDirectory() {
        return defaultDirectory;
    }
}
