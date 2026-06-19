package egovframework.admin.admin600.service;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin600.domain.Admin610DTO;
import egovframework.admin.admin600.domain.Admin610DeleteDTO;
import egovframework.admin.admin600.domain.Admin610FilterDTO;
import egovframework.admin.admin600.domain.Admin610VO;
import egovframework.common.excel.domain.ExcelExportResult;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName : Admin610Service.java
 * @Description : Q&A 관리 서비스 인터페이스
 *
 * @author : balee
 * @since  : 2025. 11. 25
 * @version : 1.0
 */
@Service
public interface Admin610Service {
    
    /**
     * Q&A 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return PageInfo<Admin610VO> 페이징된 Q&A 목록
     * @throws RuntimeException 목록 조회 중 오류 발생 시
     */
    PageInfo<Admin610VO> selectAdmin610List(Admin610FilterDTO filter);

    /**
     * Q&A 단건 조회
     * @param qnaOid 검색 조건 (Q&A번호)
     * @throws RuntimeException 조회 중 오류 발생 시
     */
    Admin610VO selectAdmin610(Long qnaOid);

    /**
     * Q&A 등록
     *
     * @param admin610DTO Q&A 정보
     * @throws RuntimeException Q&A 등록 중 오류 발생 시
     */
    void upsertAdmin610(Admin610DTO admin610DTO, List<MultipartFile> files);

    /**
     * Q&A 삭제
     *
     * @param admin610DeleteDTO Q&A 정보
     * @throws RuntimeException Q&A 삭제 중 오류 발생 시
     */
    void deleteAdmin610(Admin610DeleteDTO admin610DeleteDTO);

    /**
     * Q&A 엑셀 다운로드
     * */
    ExcelExportResult admin610ExportExcel(EgovMap cond) throws IOException;
}