package egovframework.portal.popup.mapper;

import egovframework.portal.popup.dto.PopupDTO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper("popupMapper") // Apply the specified Mapper annotation
public interface PopupMapper {

    /**
     * Popup 목록 조회
     */
    List<PopupDTO> selectPopupList();
}
