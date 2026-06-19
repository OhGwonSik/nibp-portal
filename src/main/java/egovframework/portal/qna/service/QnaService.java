package egovframework.portal.qna.service;

import com.github.pagehelper.PageInfo;
import egovframework.portal.qna.dto.QnaDTO;
import egovframework.portal.qna.dto.QnaFilter;
import egovframework.portal.qna.dto.QnaInsertDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface QnaService {
    PageInfo<?> selectQnaPostListWithFilter(QnaFilter filter);
    QnaDTO selectQnaById(Long qnaOid);
    
    QnaDTO selectQnaOneById(QnaInsertDTO qnaInsertDTO);

    /**
     * Q&A 등록
     *
     * @param qnaInsertDTO Q&A 정보
     * @throws RuntimeException Q&A 등록 중 오류 발생 시
     */
    void insertQna(QnaInsertDTO qnaInsertDTO, List<MultipartFile> files) throws IOException; 
    
    /**
     * Q&A 수정
     *
     * @param qnaInsertDTO Q&A 정보
     * @throws RuntimeException Q&A 수정 중 오류 발생 시
     */
    void updateQna(QnaInsertDTO qnaUpdateDTO, List<MultipartFile> files) throws IOException;
    
    
    /**
     * Q&A 비밀번호 일치여부
     *
     * @param qnaDTO Q&A 정보
     */
    Boolean checkQnAPassword(QnaDTO qnaDTO);
    
    /**
     * Q&A 만족도 평가 저장
     *
     * @param qnaDTO Q&A 정보
     */
    int insertSatisfactionByQna(QnaDTO qnaDTO);    
}