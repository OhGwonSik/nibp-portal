package egovframework.admin.admin1000.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : Admin1002UserSearchDTO.java
 * @Description : 사용자 검색 DTO
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
@Getter
@Setter
@ToString
public class Admin1002UserSearchDTO {
    private String keyword;        // 사용자명 또는 로그인ID
    private Integer page = 1;
    private Integer size = 20;
}
