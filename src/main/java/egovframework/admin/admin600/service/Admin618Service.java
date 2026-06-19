package egovframework.admin.admin600.service;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin600.domain.Admin618DTO;
import egovframework.admin.admin600.domain.Admin618DeleteDTO;
import egovframework.admin.admin600.domain.Admin618FilterDTO;
import egovframework.admin.admin600.domain.Admin618VO;
import egovframework.common.excel.domain.ExcelExportResult;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName : Admin618Service.java
 * @Description : 팝업존 관리 서비스 인터페이스
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
@Service
public interface Admin618Service {

    /**
     * 팝업존 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return PageInfo<Admin618VO> 페이징된 팝업존 목록
     * @throws RuntimeException 목록 조회 중 오류 발생 시
     */
    PageInfo<Admin618VO> selectAdmin618List(Admin618FilterDTO filter);

    /**
     * 팝업존 단건 조회
     * @param popupZoneOid 검색 조건 (팝업존번호)
     * @throws RuntimeException 조회 중 오류 발생 시
     */
    Admin618VO selectAdmin618(Long popupZoneOid);

    /**
     * 팝업존 등록
     *
     * @param admin618DTO 팝업존 정보
     * @throws RuntimeException 팝업존 등록 중 오류 발생 시
     */
    void insertAdmin618(Admin618DTO admin618DTO, MultipartFile thumbnailFile, List<MultipartFile> files);

    /**
     * 팝업존 수정
     *
     * @param admin618DTO 팝업존 정보
     * @throws RuntimeException 팝업존 수정 중 오류 발생 시
     */
    void updateAdmin618(Admin618DTO admin618DTO, MultipartFile thumbnailFile, List<MultipartFile> files);

    /**
     * 팝업존 삭제
     *
     * @param admin618DeleteDTO 팝업존 정보
     * @throws RuntimeException 팝업존 삭제 중 오류 발생 시
     */
    void deleteAdmin618(Admin618DeleteDTO admin618DeleteDTO);

    /**
     * 팝업존 엑셀 다운로드
     * */
    ExcelExportResult admin618ExportExcel(EgovMap cond) throws IOException;
}
