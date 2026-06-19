package egovframework.common.email.service;

import java.util.Map;

import javax.mail.MessagingException;

import egovframework.common.email.dto.EmailMessage;

/**
 * 이메일 발송을 위한 공통 서비스 인터페이스입니다.
 */
public interface EmailService {

    /**
     * 이메일을 비동기로 발송합니다.
     *
     * @param emailMessage 이메일 정보 (수신자, 제목, 내용 등)
     * @throws MessagingException 이메일 발송 중 오류 발생 시
     */
    void sendEmail(EmailMessage emailMessage) throws MessagingException;

    /**
     * Thymeleaf 템플릿을 사용하여 이메일을 동기로 발송합니다.
     * 임시 비밀번호 발급 등 발송 성공 여부를 확인해야 하는 중요한 이메일에 사용합니다.
     *
     * @param emailMessage 이메일 기본 정보 (수신자, 제목, 발신자 등). content 필드는 무시됨
     * @param templateHtml 템플릿 경로 또는 HTML 문자열 (Thymeleaf 문법 포함)
     * @param variables 템플릿에 바인딩할 변수를 담은 객체 (DTO, Entity 등)
     * @throws MessagingException 이메일 발송 중 오류 발생 시
     */
    void sendEmailWithTemplate(EmailMessage emailMessage, String templateHtml, Map<String, Object> variables) throws MessagingException;

    /**
     * Thymeleaf 템플릿을 사용하여 이메일을 비동기로 발송합니다.
     * 대용량 메일 발송 등 응답 속도가 중요한 경우에 사용합니다.
     * 발송 실패 시 로그에만 기록되므로 중요한 이메일은 동기 메서드를 사용하세요.
     *
     * @param emailMessage 이메일 기본 정보 (수신자, 제목, 발신자 등). content 필드는 무시됨
     * @param templateHtml 템플릿 경로 또는 HTML 문자열 (Thymeleaf 문법 포함)
     * @param variables 템플릿에 바인딩할 변수를 담은 객체 (DTO, Entity 등)
     * @throws MessagingException 이메일 발송 중 오류 발생 시
     */
    void sendEmailWithTemplateAsync(EmailMessage emailMessage, String templateHtml, Map<String, Object> variables) throws MessagingException;

}
