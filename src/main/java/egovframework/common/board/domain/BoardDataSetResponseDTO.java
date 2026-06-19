package egovframework.common.board.domain;

import com.github.pagehelper.PageInfo;
import egovframework.common.board.dto.BoardDTO;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDataSetResponseDTO {
    private String key;          // PRESS_RELEASE, MEDIA_TRENDS_DOMESTIC 등
    private BoardDTO board;      // 게시판 설정 정보
    private PageInfo<?> list;    // 게시글 목록
}
