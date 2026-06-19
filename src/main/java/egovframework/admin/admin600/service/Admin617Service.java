package egovframework.admin.admin600.service;

import java.io.IOException;
import java.util.List;

import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin600.domain.Admin617DTO;
import egovframework.admin.admin600.domain.Admin617DeleteDTO;
import egovframework.admin.admin600.domain.Admin617FilterDTO;
import egovframework.admin.admin600.domain.Admin617VO;
import egovframework.common.excel.domain.ExcelExportResult;

/**
 * @ClassName : Admin617Service.java
 * @Description : 카드뉴스 관리 서비스 인터페이스
 *
 * @author : balee
 * @since  : 2025. 11. 27
 * @version : 1.0
 */
@Service
public interface Admin617Service {

    /**
     * 카드뉴스 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return PageInfo<Admin617VO> 페이징된 카드뉴스 목록
     * @throws RuntimeException 목록 조회 중 오류 발생 시
     */
    PageInfo<Admin617VO> selectAdmin617List(Admin617FilterDTO filter);

    /**
     * 카드뉴스 단건 조회
     * @param cardNewsOid 검색 조건 (카드뉴스번호)
     * @throws RuntimeException 조회 중 오류 발생 시
     */
    Admin617VO selectAdmin617(Long cardNewsOid);

    /**
     * 카드뉴스 등록
     *
     * @param admin617DTO 카드뉴스 정보
     * @throws RuntimeException 카드뉴스 등록 중 오류 발생 시
     */
    void insertAdmin617(Admin617DTO admin617DTO, MultipartFile thumbnailFile, List<MultipartFile> files);

    /**
     * 카드뉴스 수정
     *
     * @param admin617DTO 카드뉴스 정보
     * @throws RuntimeException 카드뉴스 수정 중 오류 발생 시
     */
    void updateAdmin617(Admin617DTO admin617DTO, MultipartFile thumbnailFile, List<MultipartFile> files);

    /**
     * 카드뉴스 삭제
     *
     * @param admin617DeleteDTO 카드뉴스 정보
     * @throws RuntimeException 카드뉴스 삭제 중 오류 발생 시
     */
    void deleteAdmin617(Admin617DeleteDTO admin617DeleteDTO);

    /**
     * 카드뉴스 엑셀 다운로드
     * */
    ExcelExportResult admin617ExportExcel(EgovMap cond) throws IOException;
}