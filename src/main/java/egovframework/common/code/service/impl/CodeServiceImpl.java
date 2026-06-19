package egovframework.common.code.service.impl;

import egovframework.common.code.domain.CodeResponseDTO;
import egovframework.common.code.mapper.CodeMapper;
import egovframework.common.code.service.CodeService;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeServiceImpl extends EgovAbstractServiceImpl implements CodeService {

    private final CodeMapper codeMapper;

    @Override
    public List<CodeResponseDTO> getCodeList(Integer grpCdOid) {
        return codeMapper.selectCodeListByGrpCdOid(grpCdOid);
    }

    @Override
    public List<CodeResponseDTO> getCodeListByGrpCd(String grpCd) {
        return codeMapper.selectGroupCodeListByGrpCd(grpCd);
    }

    @Override
    public List<CodeResponseDTO> selectBoardTypeList() {
        String grpCd = "BOARD_TYPE";
        return codeMapper.selectGroupCodeListByGrpCd(grpCd);
    }
}
