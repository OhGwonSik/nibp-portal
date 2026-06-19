package egovframework.portal.mailing.service;

import egovframework.common.exception.BusinessException;
import egovframework.portal.mailing.domain.MailingRequestDTO;

public interface MailingService {
	/**
     * 인증번호 생성 및 이메일 발송(신청용)
     */
	public int requestMailAuth(MailingRequestDTO mailingRequestDTO) throws BusinessException;
	
	/**
     * 인증번호 생성 및 이메일 발송 (변경 / 해지용)
     */
	public int requestMailUpdateAuth(MailingRequestDTO mailingRequestDTO) throws BusinessException;
	
	/**
     * 인증번호 검증 (DB와 대조)
     */
    public boolean verifyMailAuth(MailingRequestDTO mailingRequestDTO) throws BusinessException;

    /**
     * 메일링 서비스 신청 처리
     */
    public int insertMailing(MailingRequestDTO mailingRequestDTO) throws BusinessException;
    
    /**
     * 메일링 서비스 수정 처리
     */
    public int updateMailing(MailingRequestDTO mailingRequestDTO) throws BusinessException;
    
    /**
     * 메일링 서비스 해지 처리
     */
    public int cancelMailing(MailingRequestDTO mailingRequestDTO) throws BusinessException;
    
    /**
     * 만료된(PENDING) 이메일 인증 정보를 로그 테이블로 이관 및 만료 시간이 지난 모든 이메일 인증 데이터 삭제 (Hard Delete)
     */
    void executeEmailAuthCleanup();

}
