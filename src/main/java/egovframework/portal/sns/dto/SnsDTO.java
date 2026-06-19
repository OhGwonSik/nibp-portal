package egovframework.portal.sns.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SnsDTO {
    private Long snsChnlOid;         // 고유 ID
    private String pltfmType;        // SNS 플랫폼 종류 (YOUTUBE, INSTAGRAM, FACEBOOK, BLOG)
    private String chnlNm;           // 웹사이트에 표시될 탭 이름
    private Integer dplySeq;         // 탭 표시 순서 (숫자가 낮을수록 먼저 표시)
    private String chnlUrl;          // 채널/페이지 기본 URL
    private String rssFeedUrl;       // RSS 피드 주소 (BLOG 타입 전용)
}
