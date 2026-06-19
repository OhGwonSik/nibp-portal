package egovframework.admin.admin600.service;

import java.io.IOException;
import java.util.List;

import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin600.domain.Admin606CategoryVO;
import egovframework.admin.admin600.domain.Admin606DeleteDTO;
import egovframework.admin.admin600.domain.Admin606DetailDTO;
import egovframework.admin.admin600.domain.Admin606FilterDTO;
import egovframework.admin.admin600.domain.Admin606VO;
import egovframework.common.excel.domain.ExcelExportResult;

/**
 * @ClassName : Admin606Service.java
 * @Description : FAQ 관리 서비스 인터페이스
 *
 * @author : balee
 * @since  : 2025. 11. 25
 * @version : 1.0
 */
@Service
public interface Admin606Service {

    /**
     * FAQ 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return PageInfo<Admin606DetailVO> 페이징된 FAQ 목록
     * @throws RuntimeException 목록 조회 중 오류 발생 시
     */
    PageInfo<Admin606VO> selectAdmin606List(Admin606FilterDTO filter);

    /**
     * FAQ 카테고리 목록 조회
     * @return List<Admin606CategoryVO> FAQ 카테고리 목록
     */
    List<Admin606CategoryVO> selectAdmin606CategoryList();

    /**
     * FAQ 단건 조회
     * @param faqDtlOid 검색 조건 (FAQ 번호)
     * @throws RuntimeException 조회 중 오류 발생 시
     */
    Admin606DetailDTO selectAdmin606(Long faqDtlOid);

    /**
     * FAQ 등록
     *
     * @param admin606DetailDTO FAQ 정보
     * @return Long 생성된 FAQ 번호
     * @throws RuntimeException FAQ 등록 중 오류 발생 시
     */
    Long insertAdmin606(Admin606DetailDTO admin606DetailDTO);

    /**
     * FAQ 등록
     *
     * @param admin606DetailDTO FAQ 정보
     * @throws RuntimeException FAQ 등록 중 오류 발생 시
     */
    void updateAdmin606(Admin606DetailDTO admin606DetailDTO);

    /**
     * FAQ 삭제
     *
     * @param admin606DeleteDTO FAQ 정보
     * @throws RuntimeException FAQ 삭제 중 오류 발생 시
     */
    void deleteAdmin606(Admin606DeleteDTO admin606DeleteDTO);

    /**
     * FAQ 조회수 증가
     *
     * @param faqDtlOid FAQ 번호
     * @throws RuntimeException FAQ 조회수 증가 중 오류 발생 시
     */
    void updateAdmin606inqCnt(Long faqDtlOid);

    /**
     * FAQ 엑셀 다운로드
     * */
    ExcelExportResult admin606ExportExcel(EgovMap cond) throws IOException;

    int insertFaqData(EgovMap egovMap) throws IOException;

    int updateFaqData(EgovMap egovMap) throws IOException;
}