package egovframework.common.file.vo;

import lombok.Data;

@Data
public class PostVO {
    private String postId;
    private String title;
    // tempUploadKey는 폼에서 전송된 임시 파일 ID를 받기 위한 필드
    private String tempUploadKey;
}