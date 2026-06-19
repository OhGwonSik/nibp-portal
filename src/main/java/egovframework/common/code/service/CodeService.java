package egovframework.common.code.service;

import egovframework.common.code.domain.CodeResponseDTO;

import java.util.List;

public interface CodeService {

    /**
     * group_code_no로 공통 코드 조회
     */
    List<CodeResponseDTO> getCodeList(Integer grpCdOid);

    /**
     * group_cd로 공통 코드 조회
     */
    List<CodeResponseDTO> getCodeListByGrpCd(String grpCd);

    /**
     * 게시판 타입 코드 목록 조회
     */
    List<CodeResponseDTO> selectBoardTypeList();
}