package egovframework.portal.qna.mapper;

import egovframework.portal.qna.dto.QnaDTO;
import egovframework.portal.qna.dto.QnaFilter;
import egovframework.portal.qna.dto.QnaInsertDTO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper("qnaMapper") // Apply the specified Mapper annotation
public interface QnaMapper {
    List<QnaDTO> selectQnaPostListWithFilter(QnaFilter filter);
    QnaDTO selectQnaById(Long qnaOid);

    /**
     * Q&A 등록
     * @param qnaInsertDTO Q&A 정보
     * @return
     */
    int insertQna(QnaInsertDTO qnaInsertDTO);
    
    /**
     * Q&A 수정
     * @param qnaInsertDTO Q&A 정보
     * @return
     */
    int updateQna(QnaInsertDTO qnaInsertDTO);
    int updateUpQnaUseYn(QnaInsertDTO qnaInsertDTO);
    
    /**
     * Q&A 비밀번호 확인
     * @param qnaDTO Q&A 비밀번호 정보
     * @return
     */
    QnaDTO checkQnAPassword(QnaDTO qnaDTO);
    
    /**
     * Q&A 만족도 평가 여부 확인
     * @param qnaDTO Q&A 정보
     * @return
     */
    int selectSatisfactionCountByQnaOid(QnaDTO qnaDTO);
    
    /**
     * Q&A 만족도 평가 저장
     * @param qnaDTO Q&A 만족도 평가 정보
     * @return
     */
    int updateSatisfactionCountByQnaOid(QnaDTO qnaDTO);
}