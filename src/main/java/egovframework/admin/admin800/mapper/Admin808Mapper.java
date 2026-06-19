package egovframework.admin.admin800.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import egovframework.admin.admin800.domain.Admin808DTO;
import egovframework.admin.admin800.domain.Admin808DeleteDTO;
import egovframework.admin.admin800.domain.Admin808VO;

/**
 * @ClassName : Admin808Mapper.java
 * @Description : 메뉴 관리 Mapper
 *
 * @author : balee
 * @since  : 2025. 11. 13
 * @version : 1.0
 */
@Mapper
public interface Admin808Mapper {
    /**
     * 메뉴 목록 조회
     * @return List<Admin808VO> 메뉴 목록
     */
    List<Admin808VO> selectAdmin808List();

    /**
     * 메뉴코드로 메뉴 조회
     * @param menuCd 메뉴코드
     * @return Admin808VO 메뉴 정보
     */
    Admin808VO selectByMenuCd(String menuCd);


    /**
     * 메뉴번호로 메뉴 조회
     * @param menuOid 메뉴번호
     * @return Admin808VO 메뉴 정보
     */
    Admin808VO selectByMenuNo(Long menuOid);

    /**
     * 메뉴 등록
     * @return int 등록 건수
     */
    int insertAdmin808(Admin808DTO admin808DTO);

    /**
     * 메뉴 수정
     * @return int 수정 건수
     */
    int updateAdmin808(Admin808DTO admin808DTO);

    /**
     * 메뉴 삭제
     * @return int 삭제 건수
     */
    int deleteAdmin808(Admin808DeleteDTO admin808DeleteDTO);

    /**
     * 메뉴 엑셀 다운로드
     * */
    List<Admin808VO> selectAdmin808ExcelInfo(EgovMap egovMap);
    
	/*
	 * 시퀀스 조회
	 * */
	int getNextMenuSequence(@Param("seqNm") String seqNm);
}