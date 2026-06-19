package egovframework.portal.mailing.service.impl;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import egovframework.portal.mailing.domain.MailingRequestDTO;
import egovframework.portal.mailing.mapper.MailingMapper;
import egovframework.portal.mailing.service.MailingLogService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailingLogServiceImpl extends EgovAbstractServiceImpl implements MailingLogService{
	private final MailingMapper mailingMapper;
    
	@Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)// 새 트랜잭션을 열어서 부모의 롤백에 영향을 받지 않음
    public void insertAuthLog(MailingRequestDTO mailingRequestDTO, String status) {
        mailingRequestDTO.setEmlCertSttsCd(status);
        mailingMapper.insertEmailAuthLog(mailingRequestDTO);
    }
}
