package egovframework.portal.sns.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class YoutubeDTO {
    private String videoId;      // 영상 ID
    private String title;        // 영상 제목
    private String link;         // 영상 링크
    private String thumbnail;    // 썸네일 URL
    private String channelName;  // 채널명
}
