package egovframework.common.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.web.util.HtmlUtils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

// html 처리 관련 유틸
@Slf4j
@UtilityClass
public class HtmlUtil {
    // HtmlUils를 사용해서 다중 디코딩을 하는 함수
    public static String htmlDecode(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String decoded = text;
        String previous;

        // 더 이상 변화가 없을 때까지 디코딩(안전하게 5번)
        for (int i = 0; i < 5; i++) {
            previous = decoded;
            decoded = HtmlUtils.htmlUnescape(decoded);

            if (previous.equals(decoded)) {
                break; // 이미 정상 데이터면 여기서 중단
            }
        }

        return decoded;
    }

    private static final Safelist SAFELIST = createSafelist();

    private static Safelist createSafelist() {
        return Safelist.relaxed()
                // 테이블 태그
                .addTags("caption", "colgroup", "col", "thead", "tbody", "tfoot")
                // 레이아웃/서식 태그
                .addTags("div", "span", "hr", "h1", "h2", "h3", "h4", "h5", "h6", "figure", "figcaption")
                // 공통 속성
                .addAttributes(":all", "style", "class", "id", "title")
                // 테이블 속성
                .addAttributes("table", "border", "cellpadding", "cellspacing", "width", "height", "summary")
                .addAttributes("td", "colspan", "rowspan", "width", "height", "valign", "align")
                .addAttributes("th", "colspan", "rowspan", "width", "height", "valign", "align", "scope")
                .addAttributes("col", "span", "width")
                .addAttributes("colgroup", "span")
                // 이미지 속성
                .addAttributes("img", "src", "alt", "width", "height", "loading")
                // 링크 속성
                .addAttributes("a", "href", "target", "rel")
                // 프로토콜 제한
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https", "data")
                // iframe 허용하지 않음 (보안상 제외)
                ;
    }

    public static String sanitize(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        return Jsoup.clean(html, "", SAFELIST,
                new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false));
    }

    public static String stripHtml(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        String text = Jsoup.parse(html).text();
        return text.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }
}
