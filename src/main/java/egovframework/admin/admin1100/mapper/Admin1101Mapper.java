package egovframework.admin.admin1100.mapper;

import egovframework.admin.admin1100.domain.*;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

/**
 * @ClassName : Admin1101Mapper.java
 * @Description : 정기발간자료 관리 Mapper
 *
 * @author : j.h.kim
 * @since : 2025. 01. 12
 * @version : 1.0
 */
@Mapper
public interface Admin1101Mapper {
    
    /**
     * 정기발간자료 목록 조회
     */
    List<Admin1101VO> selectAdmin1101List(Admin1101FilterDTO filter);
    
    /**
     * 정기발간자료 단건 조회
     */
    Admin1101VO selectAdmin1101(@Param("fxtmPblsDataOid") Long fxtmPblsDataOid);
    
    /**
     * 정기발간자료 섹션 목록 조회
     */
    List<Admin1101SectionVO> selectAdmin1101SectionList(@Param("fxtmPblsDataOid") Long fxtmPblsDataOid);
    
    /**
     * 정기발간자료 아이템 목록 조회
     */
    List<Admin1101ItemVO> selectAdmin1101ItemList(@Param("fxtmPblsSectOid") Long fxtmPblsSectOid);
    
    /**
     * 정기발간자료 엑셀 다운로드용 목록 조회
     */
    List<Admin1101ExcelDTO> selectAdmin1101ExcelList(Admin1101FilterDTO filter);
    
    /**
     * 정기발간자료 메인 등록
     */
    int insertAdmin1101(Admin1101VO vo);
    
    /**
     * 정기발간자료 섹션 등록
     */
    int insertAdmin1101Section(Admin1101SectionVO vo);
    
    /**
     * 정기발간자료 아이템 등록
     */
    int insertAdmin1101Item(Admin1101ItemVO vo);
    
    /**
     * 정기발간자료 메인 수정
     */
    int updateAdmin1101(Admin1101VO vo);
    
    /**
     * 정기발간자료 섹션 수정
     */
    int updateAdmin1101Section(Admin1101SectionVO vo);
    
    /**
     * 정기발간자료 아이템 수정
     */
    int updateAdmin1101Item(Admin1101ItemVO vo);
    
    /**
     * 정기발간자료 조회수 증가
     */
    int updateAdmin1101ViewCnt(@Param("fxtmPblsDataOid") Long fxtmPblsDataOid);
    
    /**
     * 정기발간자료 메인 삭제
     */
    int deleteAdmin1101(@Param("fxtmPblsDataOid") Long fxtmPblsDataOid);
    
    /**
     * 정기발간자료 섹션 삭제
     */
    int deleteAdmin1101Section(@Param("fxtmPblsSectOid") Long fxtmPblsSectOid);
    
    /**
     * 정기발간자료 아이템 삭제
     */
    int deleteAdmin1101Item(@Param("fxtmPblsItemOid") Long fxtmPblsItemOid);
    
    /**
     * 정기발간자료 섹션 전체 삭제 (periodicalNo 기준)
     */
    int deleteAdmin1101SectionByPeriodical(@Param("fxtmPblsDataOid") Long fxtmPblsDataOid);
    
    /**
     * 정기발간자료 아이템 전체 삭제 (sectionNo 기준)
     */
    int deleteAdmin1101ItemBySection(@Param("fxtmPblsSectOid") Long fxtmPblsSectOid);
}
