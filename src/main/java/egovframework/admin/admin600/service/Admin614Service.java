package egovframework.admin.admin600.service;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin600.domain.Admin614DTO;
import egovframework.admin.admin600.domain.Admin614DeleteDTO;
import egovframework.admin.admin600.domain.Admin614FilterDTO;
import egovframework.admin.admin600.domain.Admin614VO;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.file.domain.FileDTO;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName : Admin614Service.java
 * @Description : 안내(팝업) 관리 서비스 인터페이스
 *
 * @author : balee
 * @since  : 2025. 11. 24
 * @version : 1.0
 */
@Service
public interface Admin614Service {
    
    /**
     * 안내(팝업) 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return PageInfo<Admin614VO> 페이징된 안내(팝업) 목록
     * @throws RuntimeException 목록 조회 중 오류 발생 시
     */
    PageInfo<Admin614VO> selectAdmin614List(Admin614FilterDTO filter);

    /**
     * 안내(팝업) 단건 조회
     * @param popupOid 검색 조건 (안내(팝업)번호)
     * @throws RuntimeException 조회 중 오류 발생 시
     */
    Admin614VO selectAdmin614(String popupOid);

    /**
     * 안내(팝업) 등록
     *
     * @param admin614DTO 안내(팝업) 정보
     * @throws RuntimeException 안내(팝업) 등록 중 오류 발생 시
     */
    int insertAdmin614(Admin614DTO admin614DTO, MultipartFile popupFile) throws IOException;

    /**
     * 안내(팝업) 수정
     *
     * @param admin614DTO 안내(팝업) 정보
     * @throws RuntimeException 안내(팝업) 수정 중 오류 발생 시
     */
    int updateAdmin614(Admin614DTO admin614DTO, MultipartFile popupFile) throws IOException;

    /**
     * 안내(팝업) 삭제
     *
     * @param admin614DeleteDTO 안내(팝업) 정보
     * @throws RuntimeException 안내(팝업) 삭제 중 오류 발생 시
     */
    void deleteAdmin614(Admin614DeleteDTO admin614DeleteDTO);

    /**
     * 안내(팝업) 엑셀 다운로드
     * */
    ExcelExportResult admin614ExportExcel(EgovMap cond) throws IOException;

    /**
     * 안내(팝업) 첨부파일 목록 조회 (INLINE 타입 제외)
     * @param popupOid 팝업 번호
     * @return List<FileDTO> 첨부파일 목록 (INLINE 제외)
     */
    List<FileDTO> selectAdmin614AttachListByPopupOid(Long popupOid);
}