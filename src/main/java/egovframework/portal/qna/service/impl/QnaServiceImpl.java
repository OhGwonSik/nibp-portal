package egovframework.portal.qna.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.common.content.ContentProcessService;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.file.domain.FileDTO;
import egovframework.common.file.mapper.FileMapper;
import egovframework.common.file.service.FileService;
import egovframework.common.util.HtmlUtil;
import egovframework.common.util.SecurityUtil;
import egovframework.portal.qna.dto.QnaDTO;
import egovframework.portal.qna.dto.QnaFilter;
import egovframework.portal.qna.dto.QnaInsertDTO;
import egovframework.portal.qna.mapper.QnaMapper;
import egovframework.portal.qna.service.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.jsoup.Jsoup;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QnaServiceImpl extends EgovAbstractServiceImpl implements QnaService {
    private final QnaMapper qnaMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final FileMapper fileMapper;
    private final ContentProcessService contentProcessService;

    @Override
    public PageInfo<?> selectQnaPostListWithFilter(QnaFilter filter) {
        if(filter != null && filter.getPage() != null && filter.getSize() != null){
            PageHelper.startPage(filter.getPage(), filter.getSize());
        }

        return PageInfo.of(qnaMapper.selectQnaPostListWithFilter(filter));
    }
    

	@Override
	public QnaDTO selectQnaOneById(QnaInsertDTO qnaInsertDTO) {
		return qnaMapper.selectQnaById(qnaInsertDTO.getQnaOid());
	}

    @Override
    public QnaDTO selectQnaById(Long qnaOid) {
        if (qnaOid == null) {
            throw new IllegalArgumentException("qna ID는 필수입니다.");
        }

        QnaDTO qna = qnaMapper.selectQnaById(qnaOid);

        // 질문 첨부 파일 조회
        EgovMap selectParam = new EgovMap();
        selectParam.put("tblNm", "qna");
        selectParam.put("tblOid", qnaOid);
        List<FileDTO> qnaFile = fileMapper.selectAttachmentFileByTableNameAndTablePk(selectParam);
        qna.setAttachments(qnaFile);

        // 답변 첨부 파일 조회 (답변이 있을 경우에)
        if (!CollectionUtils.isEmpty(qna.getAnswers())) {
            EgovMap selectAnswerParam = new EgovMap();
            selectAnswerParam.put("tblNm", "qna");
            selectAnswerParam.put("tblOid", qna.getAnswers().get(0).getQnaOid());
            List<FileDTO> qnaAnswerFiles = fileMapper.selectAttachmentFileByTableNameAndTablePk(selectAnswerParam);
            qna.getAnswers().get(0).setAttachments(qnaAnswerFiles);
        }

        return qna;
    }

    @Transactional
    @Override
    public void insertQna(QnaInsertDTO qnaInsertDTO, List<MultipartFile> files) throws IOException {
        log.debug("insert qna");
        
        String rawpassword = qnaInsertDTO.getPswd();
        
        // 비밀번호 있을 때만 없으면 null 처리
        if(StringUtils.hasText(rawpassword)) {
            // Q&A 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(qnaInsertDTO.getPswd());
            qnaInsertDTO.setPswd(encodedPassword);
        } else {
            qnaInsertDTO.setPswd(null);
        }


        // contents_text 생성
        qnaInsertDTO.setNtcCnTxt(HtmlUtil.stripHtml(qnaInsertDTO.getNtcCn()));

        EgovMap egovMap = new EgovMap();
        populateDtoFields(egovMap, qnaInsertDTO);
        egovMap.put("path", "qna");
        egovMap.put("content", qnaInsertDTO.getNtcCn());
        egovMap.put("uploadFiles", files);
        egovMap.put("editorFiles", qnaInsertDTO.getEditorFiles());

        String userId = "SYSTEM";
        if(SecurityUtil.getAuthentication() != null && SecurityUtil.getAuthentication().isAuthenticated() && SecurityUtil.getUser() != null) {
        	userId = SecurityUtil.getUser().getUserId();
        }
        
        egovMap.put("regUserId", userId);
        egovMap.put("mdfcnId", userId);

        fileService.processFiles(egovMap);

        qnaInsertDTO.setNtcCn(contentProcessService.processHtmlContent((String) egovMap.get("content")));

        int result = qnaMapper.insertQna(qnaInsertDTO);

        if (result == 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "Q&A 저장에 실패했습니다.");
        }

        Long qnaOid = qnaInsertDTO.getQnaOid();
        if (qnaOid == null) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "Q&A 번호 생성 실패");
        }

        // file에 파일 정보 저장
        egovMap.put("tblNm", "qna");
        egovMap.put("tblOid", qnaOid);
        fileService.saveFileMeta(egovMap);
    }
    

	@Override
	public void updateQna(QnaInsertDTO qnaUpdateDTO, List<MultipartFile> files) throws IOException {
		if (qnaUpdateDTO.getQnaOid() == null) {
	        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "수정할 Q&A 번호가 없습니다.");
	    }

	    // 1. contents_text 생성 (Jsoup을 이용한 HTML 태그 제거 및 텍스트 추출)
        qnaUpdateDTO.setNtcCnTxt(HtmlUtil.stripHtml(qnaUpdateDTO.getNtcCn()));

	    // 2. 파일 처리를 위한 EgovMap 구성
	    EgovMap egovMap = new EgovMap();
	    populateDtoFields(egovMap, qnaUpdateDTO);
	    egovMap.put("path", "qna");
	    egovMap.put("content", qnaUpdateDTO.getNtcCn());
	    egovMap.put("uploadFiles", files); // 새로 추가된 첨부파일
	    egovMap.put("editorFiles", qnaUpdateDTO.getEditorFiles()); // 에디터 내 신규 이미지
	    
	    // 수정자 ID 설정
	    String userId = "SYSTEM";
	    if (SecurityUtil.getAuthentication() != null && SecurityUtil.getAuthentication().isAuthenticated() && SecurityUtil.getUser() != null) {
	        userId = SecurityUtil.getUser().getUserId();
	    }
	    qnaUpdateDTO.setMdfcnId(userId);
	    egovMap.put("mdfcnId", userId);

	    // 3. 파일 서비스 호출 (임시 파일 이동 및 본문 내 이미지 경로 치환)
	    fileService.processFiles(egovMap);

	    // 정규화된 본문 내용을 DTO에 다시 세팅
	    qnaUpdateDTO.setNtcCn(contentProcessService.processHtmlContent((String) egovMap.get("content")));

	    // 4. DB 정보 업데이트
	    int result = qnaMapper.updateQna(qnaUpdateDTO);

	    if (result == 0) {
	        throw new BusinessException(ErrorCode.DATABASE_ERROR, "Q&A 수정에 실패했습니다.");
	    }

	    // 5. file 메타 정보 반영 (신규 파일 등록)
	    egovMap.put("tblNm", "qna");
	    egovMap.put("tblOid", qnaUpdateDTO.getQnaOid());
	    fileService.saveFileMeta(egovMap);

	    // 6. 삭제된 파일 처리 (기존 첨부파일 및 에디터 이미지 물리 삭제)
	    List<Long> deleteAttachNos = qnaUpdateDTO.getDeleteAttachNos();
	    List<Long> deleteEditorAttachNos = qnaUpdateDTO.getDeleteEditorAttachNos();

	    if (!CollectionUtils.isEmpty(deleteAttachNos)) {
	        fileService.deleteFilesByFileNos(deleteAttachNos, userId);
	    }
	    if (!CollectionUtils.isEmpty(deleteEditorAttachNos)) {
	        fileService.deleteFilesByFileNos(deleteEditorAttachNos, userId);
	    }
		
	}

    private void populateDtoFields(EgovMap target, QnaInsertDTO dto) {
        target.put("qnaOid", dto.getQnaOid());
        target.put("parentNo", dto.getUpQnaOid());
        target.put("userId", dto.getUserId());
        target.put("writerNm", dto.getWrtrNm());
        target.put("pswd", dto.getPswd());
        target.put("title", dto.getQnaTtl());
        target.put("content", dto.getNtcCn());
        target.put("contentText", dto.getNtcCnTxt());
        target.put("answerYn", dto.getAnsYn());
        target.put("secretYn", dto.getPrvtPstYn());
        target.put("useYn", dto.getUseYn());
        target.put("viewCnt", dto.getInqCnt());
        target.put("regDt", dto.getRegDt());
        target.put("updDt", dto.getMdfcnDt());
    }

	@Override
	public Boolean checkQnAPassword(QnaDTO qnaDTO) {
		
		QnaDTO qnaPwdInfo = qnaMapper.checkQnAPassword(qnaDTO);
		if (qnaPwdInfo == null) {
	        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "비밀번호가 존재하지 않습니다.");
	    }
		
		// 저장되어있는 비밀번호 (암호화되어있음)
		String dbPwd = qnaPwdInfo.getPswd();
		// 사용자가 입력한 비밀번호
		String userInputPwd = qnaDTO.getPswd();
		
		//저장된 패스워드 / 입력한 패스워드 비교
		if(passwordEncoder.matches(userInputPwd, dbPwd)) {
			return true;
		}
		
		return false;
	}

	@Override
	public int insertSatisfactionByQna(QnaDTO qnaDTO) {
		int count = 0;
		
		String updId = "SYSTEM";
    	if(SecurityUtil.getAuthentication() != null && SecurityUtil.getAuthentication().isAuthenticated() && SecurityUtil.getUser() != null) {
			updId = SecurityUtil.getUser().getUserId();
		}
    	
    	qnaDTO.setTblNm("qna");
    	qnaDTO.setTblOid(qnaDTO.getQnaOid());
    	
    	int result = qnaMapper.selectSatisfactionCountByQnaOid(qnaDTO);
    	if(result > 0) {
	        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미 만족도 평가를 완료하셨습니다.");
    	}
		
    	qnaDTO.setUpdId(updId);
    	qnaDTO.setStts("COMPLETED");
    	count = qnaMapper.updateSatisfactionCountByQnaOid(qnaDTO);
    	if(count == 0) {
    		throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "만족도 평가 저장 에러");
    	}
		return count;
	}



}
