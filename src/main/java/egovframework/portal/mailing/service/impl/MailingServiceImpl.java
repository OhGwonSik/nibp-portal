package egovframework.portal.mailing.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.mail.MessagingException;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.common.component.AESComponent;
import egovframework.common.email.dto.EmailMessage;
import egovframework.common.email.service.EmailService;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.util.CryptoUtil;
import egovframework.portal.mailing.domain.MailingRequestDTO;
import egovframework.portal.mailing.mapper.MailingMapper;
import egovframework.portal.mailing.service.MailingLogService;
import egovframework.portal.mailing.service.MailingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailingServiceImpl extends EgovAbstractServiceImpl implements MailingService{
	private final MailingMapper mailingMapper;
	private final EmailService emailService;
	private final AESComponent aesComponent;
	private final CryptoUtil cryptoUtil;
	private final MailingLogService mailingLogService;
	
	@Value("${mailing.auth.characters}")
    private String authCharacters;
	
	// 관리자 수신자 리스트
    @Value("${mailing.notification.receivers}")
    private List<String> adminReceivers;
    
	@Override
	public int requestMailAuth(MailingRequestDTO mailingRequestDTO) throws BusinessException {
		int count = 0;
		mailingRequestDTO.setSecretKey(aesComponent.getSecretKey());
		
		mailingRequestDTO.splitMpno();
		
		String mailLocal = mailingRequestDTO.getEmlLcal();
        
        // 6자리 인증코드 생성
        String authCode = generateAlphanumericCode(6);
        mailingRequestDTO.setCertCd(authCode);
        
        
        count = mailingMapper.insertEmailAuth(cryptoUtil.encrypt(mailingRequestDTO));
        if(count == 0) {
        	throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이메일 인증 정보 저장 실패");
        }

        // 이메일 발송 처리
        String toEmail = mailLocal + "@" + mailingRequestDTO.getEmlDmn();
        
        String modeName = "신청";
        if ("CHANGE".equals(mailingRequestDTO.getMode())) {
            modeName = "변경";
        } else if ("CANCEL".equals(mailingRequestDTO.getMode())) {
            modeName = "해지";
        }
        
        String subject = "[국가생명윤리정책원] 메일링 서비스 " + modeName + " 인증번호 안내";
        
        EmailMessage emailMessage = EmailMessage.builder()
                .to(new String[]{toEmail})
                .subject(subject)
                .build();

        Map<String, Object> variables = new HashMap<>();
        variables.put("userNm", mailingRequestDTO.getUserNm());
        variables.put("authCode", authCode);

        try {
			emailService.sendEmailWithTemplateAsync(emailMessage, "email/mail_auth", variables);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
        
        return count;
	}
	
	@Override
	public int requestMailUpdateAuth(MailingRequestDTO mailingRequestDTO) throws BusinessException {
	    int count = 0;
	    mailingRequestDTO.setSecretKey(aesComponent.getSecretKey());
	    mailingRequestDTO.splitMpno();
	    
	    // 평문 정보
	    String rawUserNm = mailingRequestDTO.getUserNm();
	    String rawMpnoFull = mailingRequestDTO.getMpnoFull();
	    String oldLocal = mailingRequestDTO.getEmlLcal();      // 기존 메일 (조회용)
	    String oldDmn = mailingRequestDTO.getEmlDmn();
	    String newLocal = mailingRequestDTO.getNewEmlLocal();   // 신규 메일 (변경 대상)
	    String newDmn = mailingRequestDTO.getNewEmlDmn();

	    // DTO 평문 복구
	    mailingRequestDTO.setUserNm(rawUserNm);
	    mailingRequestDTO.setMpnoFull(rawMpnoFull);
	    mailingRequestDTO.splitMpno();

	    // 인증 대상 설정
	    String targetLocal;
	    String targetDmn;
	    String modeName = "해지";

	    if ("CHANGE".equals(mailingRequestDTO.getMode())) {
	        targetLocal = newLocal;
	        targetDmn = newDmn;
	        modeName = "변경";
	    } else {
	        targetLocal = oldLocal;
	        targetDmn = oldDmn;
	    }

	    mailingRequestDTO.setEmlLcal(targetLocal);
	    mailingRequestDTO.setEmlDmn(targetDmn);
	    
	    // 6자리 코드를 생성하고 암호화하여 저장
	    String authCode = generateAlphanumericCode(6);
	    mailingRequestDTO.setCertCd(authCode);

	    count = mailingMapper.insertEmailAuth(cryptoUtil.encrypt(mailingRequestDTO));
	    if(count == 0) {
	        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이메일 인증 정보 저장 실패");
	    }

	    // 평문 대상 주소로 인증 메일을 발송
	    String toEmail = targetLocal + "@" + targetDmn;
	    
	    EmailMessage emailMessage = EmailMessage.builder()
	            .to(new String[]{toEmail})
	            .subject("[국가생명윤리정책원] 메일링 서비스 " + modeName + " 인증번호 안내")
	            .build();

	    Map<String, Object> variables = new HashMap<>();
	    variables.put("userNm", rawUserNm);
	    variables.put("authCode", authCode);
	    variables.put("mode", modeName);

	    try {
	        emailService.sendEmailWithTemplateAsync(emailMessage, "email/mail_auth", variables);
	    } catch (MessagingException e) {
	        e.printStackTrace();
	    }
	    
	    return count;
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean verifyMailAuth(MailingRequestDTO mailingRequestDTO) throws BusinessException {
		
		mailingRequestDTO.setSecretKey(aesComponent.getSecretKey());
		
		// 이메일 인증 정보 내역 가져오기
		MailingRequestDTO authInfo = mailingMapper.selectEmailAuthInfo(cryptoUtil.encrypt(mailingRequestDTO));
		
		String finalLogStat = "FAIL";
		
		try {
	        // 인증 기록 존재 여부
	        if (authInfo == null) {
	            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "인증 요청 기록이 없습니다. 인증번호를 다시 요청해 주세요.");
	        }

	        // 인증번호 일치 여부
	        if (!authInfo.getCertCd().equals(mailingRequestDTO.getCertCd())) {
	            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "인증번호가 일치하지 않습니다. 다시 확인해 주세요.");
	        }

	        // 만료 시간 확인
	        if (authInfo.getExpireDt().isBefore(LocalDateTime.now())) {
	            finalLogStat = "EXPIRED";
	            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "인증 시간이 만료되었습니다. 인증번호를 재발송해 주세요.");
	        }

	        finalLogStat = "SUCCESS";
	        
	        // eml_cert 상태를 USED로 변경
	        mailingMapper.updateEmailAuthStatusUsed(authInfo.getEmlCertOid());
	        
	        return true;

	    } catch (BusinessException e) {
	        throw e;
	    } finally {
	        // 이메일 인증 로그 테이블에 insert (트랜잭션떄문에 다른 service 호출)
	    	mailingLogService.insertAuthLog(mailingRequestDTO, finalLogStat);
	    }
	}

	@Override
	public int insertMailing(MailingRequestDTO mailingRequestDTO) throws BusinessException {
		if (mailingRequestDTO.getUserNm() == null || mailingRequestDTO.getEmlLcal() == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "필수 신청 정보가 누락되었습니다.");
        }

        mailingRequestDTO.splitMpno();
        
        sendAdminAlertEmail("SIGNUP", mailingRequestDTO);
        
        return 1;
	}
	
	@Override
	public int updateMailing(MailingRequestDTO mailingRequestDTO) throws BusinessException {
		mailingRequestDTO.splitMpno();
        
        sendAdminAlertEmail("CHANGE", mailingRequestDTO);
        
        return 1;
	}
	
	@Override
	public int cancelMailing(MailingRequestDTO mailingRequestDTO) throws BusinessException {
		mailingRequestDTO.splitMpno();
        
        // 관리자 알림 메일 발송 (CANCEL)
        sendAdminAlertEmail("CANCEL", mailingRequestDTO);
        
        return 1;
	}
	
	/**
     * 관리자 알림 메일 발송 메서드
     */
    private void sendAdminAlertEmail(String mode, MailingRequestDTO dto) {
        String titlePrefix = "";
        
        if ("SIGNUP".equals(mode)) titlePrefix = "신청";
        else if ("CHANGE".equals(mode)) titlePrefix = "변경";
        else if ("CANCEL".equals(mode)) titlePrefix = "해지";
        
        // 소속/직위 Null 처리
        String safeOrg = (dto.getInstNm() != null) ? dto.getInstNm() : "";
        String safePstn = (dto.getJbpsNm() != null) ? dto.getJbpsNm() : "";

        // 제목 포맷 - ([국가생명윤리정책원 메일 서비스 신청] 홍길동 / 정보화운영팀, 전산원)
        String subject = String.format("[국가생명윤리정책원 메일 서비스 %s] %s / %s, %s", 
                titlePrefix, dto.getUserNm(), safeOrg, safePstn);

        // 수신자 목록 변환
        if (adminReceivers == null || adminReceivers.isEmpty()) {
            log.info("메일링 관리자 수신자(mailing.notification.receivers)가 설정되지 않았습니다.");
        }
        String[] receivers = adminReceivers != null ? adminReceivers.toArray(new String[0]) : new String[0];

        // 템플릿 변수 매핑
        Map<String, Object> variables = new HashMap<>();
        variables.put("mode", mode);
        variables.put("userNm", dto.getUserNm());
        variables.put("mpnoFull", dto.getMpnoFull());
        variables.put("orgNm", safeOrg);
        variables.put("pstnNm", safePstn);

        if ("CHANGE".equals(mode)) {
            String oldEmail = dto.getEmlLcal() + "@" + dto.getEmlDmn();
            String newEmail = dto.getNewEmlLocal() + "@" + dto.getNewEmlDmn();
            
            variables.put("oldEmlFull", oldEmail);
            variables.put("emlFull", newEmail);
        } else {
            String currentEmail = dto.getEmlLcal() + "@" + dto.getEmlDmn();
            variables.put("emlFull", currentEmail);
        }

        // 메일 발송
        EmailMessage emailMessage = EmailMessage.builder()
                .to(receivers)
                .from("국가생명윤리정책원 <noreply@nibp.kr>")
                .subject(subject)
                .build();

        try {
            emailService.sendEmailWithTemplateAsync(emailMessage, "email/mailing_alert", variables);
            log.info("관리자 알림 메일 발송 완료 (Mode: {}, User: {})", mode, dto.getUserNm());
        } catch (MessagingException e) {
            log.error("관리자 알림 메일 발송 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "관리자에게 신청 정보를 전송하는 중 오류가 발생했습니다.");
        }
    }	

	@Override
	public void executeEmailAuthCleanup() {
		// 만료된 PENDING 로그 기록
        int logCount = mailingMapper.insertExpiredEmailAuthLogs();
        if (logCount > 0) {
            log.info("만료된 미인증(PENDING) 데이터 {}건 로그 기록 완료", logCount);
        }

        // 만료된 모든 인증 데이터 삭제
        int deleteCount = mailingMapper.deleteExpiredEmailAuth();
        log.info("만료된 인증 데이터 총 {}건 삭제 완료", deleteCount);
		
	}
	
	/**
     * 알파벳 대문자와 숫자를 혼합한 난수 생성
     */
    private String generateAlphanumericCode(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(authCharacters.length());
            sb.append(authCharacters.charAt(index));
        }

        return sb.toString();
    }
}
