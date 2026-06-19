package egovframework.admin.admin800.service;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin807DTO;
import egovframework.admin.admin800.domain.Admin807DeleteDTO;
import egovframework.admin.admin800.domain.Admin807FilterDTO;
import egovframework.admin.admin800.domain.Admin807VO;
import egovframework.common.excel.domain.ExcelExportResult;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.io.IOException;

/**
 * @ClassName : Admin807Service.java
 * @Description : 게시판 관리 서비스 인터페이스
 *
 * @author : 
 * @since  : 2025. 11. 14
 * @version : 1.0
 */
public interface Admin807Service {

    /**
     * 게시판 목록 조회
     * @param filter 검색 및 페이징 조건
     * @return PageInfo<Admin807VO> 페이징된 게시판 목록
     * @throws RuntimeException 목록 조회 중 오류 발생 시
     */
    PageInfo<Admin807VO> selectBoardList(Admin807FilterDTO filter);

    /**
     * 게시판 등록
     *
     * @param admin807DTO 게시판 정보
     * @throws RuntimeException 게시판 등록 중 오류 발생 시
     */
    int insertBoard(Admin807DTO admin807DTO);


    /**
     * 게시판 수정
     *
     * @param admin807DTO 게시판 정보
     * @throws RuntimeException 게시판 수정 중 오류 발생 시
     */
    int updateBoard(Admin807DTO admin807DTO);

    /**
     * 게시판 삭제 (soft delete)
     *
     * @param admin807DeleteDTO 게시판 정보
     * @throws RuntimeException 게시판 삭제 중 오류 발생 시
     */
    int deleteBoard(Admin807DeleteDTO admin807DeleteDTO);

    /**
     * 게시판 엑셀 다운로드
     * */
    ExcelExportResult admin807ExportExcel(EgovMap cond) throws IOException;
}
