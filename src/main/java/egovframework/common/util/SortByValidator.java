package egovframework.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * PageHelper sortBy 파라미터에 대한 SQL Injection 방지 유틸리티.
 * 허용된 컬럼명과 정렬 방향(ASC/DESC)만 통과시킨다.
 */
public class SortByValidator {

    private static final Pattern DIRECTION = Pattern.compile("^(ASC|DESC)$", Pattern.CASE_INSENSITIVE);

    private SortByValidator() {}

    /**
     * sortBy 문자열을 검증하여 허용된 컬럼만 통과시킨다.
     *
     * @param sortBy       사용자 입력 sortBy (예: "notice_no DESC, reg_dt ASC")
     * @param allowedColumns 허용 컬럼 Set (예: Set.of("notice_no", "n.notice_no", "reg_dt"))
     * @param defaultSort  검증 실패 시 반환할 기본값
     * @return 검증된 sortBy 문자열 또는 기본값
     */
    public static String sanitize(String sortBy, Set<String> allowedColumns, String defaultSort) {
        if (sortBy == null || sortBy.isBlank()) {
            return defaultSort;
        }

        String[] parts = sortBy.split(",");
        List<String> validParts = new ArrayList<>();

        for (String part : parts) {
            String trimmed = part.trim();
            String[] tokens = trimmed.split("\\s+");

            if (tokens.length == 1) {
                // 방향 없이 컬럼만 있는 경우 (예: "sort_order")
                if (!allowedColumns.contains(tokens[0])) {
                    return defaultSort;
                }
                validParts.add(tokens[0]);
            } else if (tokens.length == 2) {
                // 컬럼 + 방향 (예: "notice_no DESC")
                String column = tokens[0];
                String direction = tokens[1];
                if (!allowedColumns.contains(column) || !DIRECTION.matcher(direction).matches()) {
                    return defaultSort;
                }
                validParts.add(column + " " + direction.toUpperCase());
            } else {
                return defaultSort;
            }
        }

        if (validParts.isEmpty()) {
            return defaultSort;
        }

        return String.join(", ", validParts);
    }
}
