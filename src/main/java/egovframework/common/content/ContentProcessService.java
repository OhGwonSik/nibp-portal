package egovframework.common.content;

public interface ContentProcessService {

    /**
     * HTML 컨텐츠 내의 <img> 태그를 처리합니다.
     * src가 "_m"으로 끝나지 않는 이미지를 찾아 관련 로직을 수행합니다.
     * @param htmlContent 원본 HTML 문자열
     * @return 처리된 HTML 문자열
     */
    String processHtmlContent(String htmlContent);
}
