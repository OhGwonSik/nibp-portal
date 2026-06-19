package egovframework.admin.admin800.service;

import egovframework.admin.admin800.domain.Admin808DTO;
import egovframework.admin.admin800.domain.Admin808DeleteDTO;
import egovframework.common.excel.domain.ExcelExportResult;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName : Admin808Service.java
 * @Description : 메뉴 관리 서비스 인터페이스
 *
 * @author : balee
 * @since  : 2025. 11. 13
 * @version : 1.0
 */
public interface Admin808Service {
    /**
     * 메뉴 목록 조회
     */
    List<Admin808DTO> selectAdmin808List();

    /**
     * 메뉴 등록
     */
    int insertAdmin808(Admin808DTO admin808DTO);

    /**
     * 메뉴 수정
     */
    int updateAdmin808(Admin808DTO admin808DTO);

    /**
     * 메뉴 삭제
     */
    int deleteAdmin808(List<Admin808DeleteDTO> admin808DeleteDTOS);
    
    /**
     * 메뉴 엑셀 다운로드
     * */
    ExcelExportResult admin808ExportExcel(EgovMap cond) throws IOException;
    
    /*
     * 시퀀스 조회
     * */
    int getNextMenuSequence();
}