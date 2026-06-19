package egovframework.common.component;

import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Component;

import java.util.Map;
/**
 * @ClassName : EgovMapComponent.java
 * @Description : EgovMap 변환 Component
 *
 * @author : tspark
 * @since  : 2025. 10. 27
 * @version : 1.0
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일              수정자               수정내용
 *  -------------  ------------   ---------------------
 *   2025. 10. 27    tspark               최초 생성
 * </pre>
 *
 */
@Component
public class EgovMapComponent {
    public EgovMap convertToEgovMap(Map<String, Object> map) {
        EgovMap egovMap = new EgovMap();
        egovMap.putAll(map);
        
        return egovMap;
    }
}
