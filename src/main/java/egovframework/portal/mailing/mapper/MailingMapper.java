package egovframework.portal.mailing.mapper;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.portal.mailing.domain.MailingRequestDTO;

@Mapper
public interface MailingMapper {
	/**
     * 메일링 신청 유무 확인
     * 이름, 전화번호, 이메일 정보를 바탕으로 현재 구독 중인 사용자 카운트 조회
     */
    int selectActiveSubscriber(MailingRequestDTO mailingRequestDTO);

    /**
     * 이메일 인증 정보 저장
     * 이메일(암호화), 인증코드, 만료시간 등을 저장
     */
    int insertEmailAuth(MailingRequestDTO mailingRequestDTO);
    
    /**
     * 이메일 인증내역 조회 (만료기간 최신순 1개 [PENDING])
     * */
    MailingRequestDTO selectEmailAuthInfo(MailingRequestDTO mailingRequestDTO);
    
    /**
     * 이메일 인증 상태 변경 (PENDING -> USED)
     */
    int updateEmailAuthStatusUsed(Long authNo);

    /**
     * 이메일 인증 로그 생성 (SUCCESS, FAIL, EXPIRED)
     */
    int insertEmailAuthLog(MailingRequestDTO mailingRequestDTO);
    
    /**
     * 메일링 정보 신청
     */
    int insertMailingInfo(MailingRequestDTO mailingRequestDTO);
    
    /**
     * 메일링 정보 수정
     * */
    int updateSubscriber(MailingRequestDTO mailingRequestDTO);
    
    /**
     * 메일링 정보 해지
     * */
    int cancelSubscriber(MailingRequestDTO mailingRequestDTO);
    
    String getMailingNextWithdrawSeq();
    
    /**
     * 만료된(PENDING) 이메일 인증 정보를 로그 테이블로 이관
     * @return 이관된 행의 수
     */
    int insertExpiredEmailAuthLogs();

    /**
     * 만료 시간이 지난 모든 이메일 인증 데이터 삭제 (Hard Delete)
     * @return 삭제된 행의 수
     */
    int deleteExpiredEmailAuth();
}
