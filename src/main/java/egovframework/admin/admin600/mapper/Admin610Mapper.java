package egovframework.admin.admin600.mapper;

import egovframework.admin.admin600.domain.*;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.util.List;

/**
 * @ClassName : Admin610Mapper.java
 * @Description : Q&A 관리 관련 Mapper
 *
 * @author : balee
 * @since  : 2025. 11. 25
 * @version : 1.0
 */
@Mapper
public interface Admin610Mapper {

	/**
	 * Q&A 목록 조회
	 * @param filter 검색 및 페이징 조건
	 * @return List<Admin610VO> Q&A 목록
	 */
	List<Admin610VO> selectAdmin610List(Admin610FilterDTO filter);

	/**
	 * Q&A 단건 조회
	 * @param qnaOid 검색조건(질문 Q&A번호)
	 * @return List<Admin810VO> Q&A 목록
	 */
	Admin610VO selectAdmin610(Long qnaOid);

	/**
	 * Q&A 등록
	 * @param admin610DTO Q&A 정보
	 * @return
	 */
	int insertAdmin610(Admin610DTO admin610DTO);
	
	/**
	 * Q&A 비밀글 여부 확인
	 * @param admin610DTO Q&A 정보
	 * @return String (Y/N)
	 */
	String selectParentSecretYn(Admin610DTO admin610DTO);
	
	/**
	 * Q&A 만족도평가 insert : 디폴트 미응답
	 * @param admin610DTO Q&A 정보
	 * @return int
	 */
	int insertQnaSatisfaction(Admin610DTO admin610DTO);

	/**
	 * Q&A 수정
	 * @param admin610DTO Q&A 정보
	 * @return
	 */
	int updateAdmin610(Admin610DTO admin610DTO);

	/**
	 * Q&A 삭제
	 * @param admin610DeleteDTO Q&A 정보
	 * @return
	 */
	int deleteAdmin610(Admin610DeleteDTO admin610DeleteDTO);
	
	/**
	 * Q&A 만족도 평가 삭제
	 * @param admin610DeleteDTO Q&A 정보
	 * @return
	 */
	int deleteAdmin610QnaSatisfaction(Admin610DeleteDTO admin610DeleteDTO);	

	/**
	 * Q&A 조회수 증가
	 * @param qnaOid 질문 Q&A번호
	 * @return
	 */
	int updateAdmin610inqCnt(Long qnaOid);

	/**
	 * Q&A 답변여부 수정
	 * @param qnaOid 질문 Q&A번호
	 * @return
	 */
	int updateAdmin610AnswerYn(Long qnaOid);

	/**
	 * Q&A 엑셀 다운로드용 목록 조회
	 * @param cond 검색 조건
	 * @return List<Admin610ExcelDTO> Q&A 목록
	 */
	List<Admin610ExcelDTO> selectAdmin610ExcelList(EgovMap cond);
}
