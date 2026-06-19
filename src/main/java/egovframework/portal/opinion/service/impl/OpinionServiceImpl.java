package egovframework.portal.opinion.service.impl;

import java.io.IOException;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;

import egovframework.common.content.ContentProcessService;
import egovframework.common.exception.BusinessException;
import egovframework.common.util.HtmlUtil;
import egovframework.common.exception.ErrorCode;
import egovframework.common.file.service.FileService;
import egovframework.portal.opinion.domain.PublicDataOpinionSaveDTO;
import egovframework.portal.opinion.mapper.OpinionMapper;
import egovframework.portal.opinion.service.OpinionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpinionServiceImpl extends EgovAbstractServiceImpl implements OpinionService {

	private final OpinionMapper opinionMapper;
	private final FileService fileService;
	private final ContentProcessService contentProcessService;
	
	@Override
	public int insertPublicDataOpinion(PublicDataOpinionSaveDTO publicDataOpinionSaveDTO) throws RuntimeException, IOException {
		int count = 0;
		
		String userId = "SYSTEM";
		
		EgovMap egovMap = new EgovMap();
        egovMap.put("tblNm", "pbl_data_opnn");
        egovMap.put("path", "opinion");
        egovMap.put("content",  publicDataOpinionSaveDTO.getPblDataOpnnCn());
        egovMap.put("editorFiles", publicDataOpinionSaveDTO.getEditorFiles());
        egovMap.put("regUserId", userId);
        egovMap.put("mdfcnId", userId);

        fileService.processFiles(egovMap);

        publicDataOpinionSaveDTO.setPblDataOpnnCn(contentProcessService.processHtmlContent((String) egovMap.get("content")));

        // contents_text 생성
        publicDataOpinionSaveDTO.setPblDataOpnnCnTxt(HtmlUtil.stripHtml(publicDataOpinionSaveDTO.getPblDataOpnnCn()));


        count = opinionMapper.insertPublicDataOpinion(publicDataOpinionSaveDTO);
        if(count == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "공공데이터 의견수렴 저장 에러");
        }

        egovMap.put("tblOid", publicDataOpinionSaveDTO.getPblDataOpnnOid());
        fileService.saveFileMeta(egovMap);
        
		return count;
	}

}
