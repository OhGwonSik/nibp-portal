package egovframework.portal.mailing.service;

import egovframework.portal.mailing.domain.MailingRequestDTO;

public interface MailingLogService {
	/**
     * 이메일 인증 로그 저장
     */
    void insertAuthLog(MailingRequestDTO mailingRequestDTO, String status);
}
