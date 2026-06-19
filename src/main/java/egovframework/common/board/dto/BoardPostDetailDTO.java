package egovframework.common.board.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class BoardPostDetailDTO extends BoardPostDTO {

    private List<BoardAttachDTO> attachList;
    private List<BoardCommentDTO> commentList;

}
