package egovframework.admin.admin1100.service;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin1100.domain.*;
import egovframework.common.excel.domain.ExcelExportResult;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @ClassName : Admin1101Service.java
 * @Description : 정기발간자료 관리 Service
 *
 * @author : j.h.kim
 * @since : 2025. 01. 12
 * @version : 1.0
 */
public interface Admin1101Service {
    
    /**
     * 정기발간자료 목록 조회
     */
    PageInfo<Admin1101VO> selectAdmin1101List(Admin1101FilterDTO filter);
    
    /**
     * 정기발간자료 상세 조회 (메인 + 섹션 + 아이템)
     */
    Admin1101DetailDTO selectAdmin1101Detail(Long fxtmPblsDataOid);
    
    /**
     * 정기발간자료 등록
     */
    Long insertAdmin1101(Admin1101SaveDTO dto, String userId);
    
    /**
     * 정기발간자료 수정
     */
    int updateAdmin1101(Admin1101SaveDTO dto, String userId);
    
    /**
     * 정기발간자료 삭제
     */
    int deleteAdmin1101(Admin1101DeleteDTO dto);
    
    /**
     * 정기발간자료 엑셀 다운로드
     */
    ExcelExportResult excelDownload(Admin1101FilterDTO filter);

    /**
     * 파일 업로드 및 저장
     * 
     * @param userOid 사용자 번호
     * @param file 업로드할 파일
     * @param fileType 파일 타입 (COVER, FULL, SPECIAL, PAPER, APPENDIX)
     * @return 저장된 파일 번호
     */
    Long saveFile(Long userOid, MultipartFile file, String fileType) throws IOException;
}
