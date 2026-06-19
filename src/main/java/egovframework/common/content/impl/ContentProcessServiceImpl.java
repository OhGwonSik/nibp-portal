package egovframework.common.content.impl;

import egovframework.common.content.ContentProcessService;
import egovframework.common.util.HtmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ContentProcessServiceImpl extends EgovAbstractServiceImpl implements ContentProcessService {

    @Override
    /**
     * jsoup를 사용해 img 태그의 이미지들을 리사이징 된 객체로 변경한다.
     * 파일명이 "_m"으로 끝나지 않은 이미지에 대해 "_m" 접미사를 추가한 리사이징 이미지 경로로 변경한다.
     * 예: image.jpg -> image_m.jpg
     */
    public String processHtmlContent(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent;
        }

        // XSS 방지: 악의적인 스크립트 태그/속성 제거
        htmlContent = HtmlUtil.sanitize(htmlContent);

        Document doc = Jsoup.parseBodyFragment(htmlContent);
        Elements images = doc.select("img");

        for (Element img : images) {
            String src = img.attr("src");

            if (src != null && !src.isEmpty()) {
                // 파일명 추출 (경로의 마지막 / 이후 부분)
                int slashIndex = src.lastIndexOf('/');
                String fileName = slashIndex >= 0 ? src.substring(slashIndex + 1) : src;

                // 확장자 위치 찾기
                int dotIndex = fileName.lastIndexOf('.');

                // 확장자가 있고, 파일명(확장자 제외)이 "_m"으로 끝나지 않는 경우만 처리
                // 예: image.jpg (O), image_m.jpg (X), album_m.jpg (X)
                if (dotIndex > 0) {
                    String fileNameWithoutExt = fileName.substring(0, dotIndex);

                    if (!fileNameWithoutExt.endsWith("_m")) {
                        log.info("모바일 이미지 처리가 필요한 이미지 발견: {}", src);

                        int srcDotIndex = src.lastIndexOf('.');
                        String newSrc = src.substring(0, srcDotIndex) + "_m" + src.substring(srcDotIndex);

                        log.info("이미지 src를 변경합니다: {} -> {}", src, newSrc);
                        img.attr("src", newSrc);
                    }
                }
            }
        }

        return doc.body().html();
    }
}
