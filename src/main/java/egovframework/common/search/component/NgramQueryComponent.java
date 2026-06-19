package egovframework.common.search.component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class NgramQueryComponent {

    /**
     * 검색어를 MariaDB FTS 불리언 모드에 적합한 2-gram 쿼리 문자열로 변환
     * * @param searchTerm 사용자가 입력한 검색어
     * @param nSize N-gram 크기 (기본값: 2)
     * @return MariaDB FTS 쿼리 문자열
     */
	public static String generateNgramQuery(String searchTerm, int nSize) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return "";
        }

        String[] words = searchTerm.trim().split("\\s+");
        Set<String> ngramTokens = new HashSet<>();
        
        for (String word : words) {
            // 1. 영어, 숫자, 기호로만 된 단어 처리
            if (word.matches("^[a-zA-Z0-9\\p{Punct}]+$")) {
                ngramTokens.add(word);
            } else {
                // 2. 한글 등 N-gram 분할 처리
                if (word.length() < nSize) {
                    ngramTokens.add(word);
                } else {
                    for (int i = 0; i <= word.length() - nSize; i++) {
                        ngramTokens.add(word.substring(i, i + nSize));
                    }
                }
            }
        }

        // 앞에 '+'를 붙이지 않고 공백으로만 연결
        // 기본적으로 'OR' 조건으로 검색
        return ngramTokens.stream()
                .collect(Collectors.joining(" "));
    }

    public static String generateNgramQuery(String searchTerm) {
        return generateNgramQuery(searchTerm, 2);
    }
}