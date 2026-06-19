package egovframework.common.email.service.impl;

import java.util.Arrays;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import egovframework.common.email.dto.EmailMessage;
import egovframework.common.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl extends EgovAbstractServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    // private final ObjectMapper objectMapper;

    @Value("${spring.mail.username}")
    private String defaultFrom;

    @Override
    @Async("asyncExecutor")
    public void sendEmail(EmailMessage emailMessage) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // 발신자 설정 (지정되지 않은 경우 기본값 사용)
            String from = StringUtils.hasText(emailMessage.from()) ? emailMessage.from() : defaultFrom;
            helper.setFrom(from);

            // 수신자, 제목, 내용 설정
            helper.setTo(emailMessage.to());
            helper.setSubject(emailMessage.subject());
            helper.setText(emailMessage.content(), true); // true for HTML content

            // 첨부파일 추가
            if (emailMessage.attachments() != null && !emailMessage.attachments().isEmpty()) {
                for (java.io.File file : emailMessage.attachments()) {
                    FileSystemResource fsr = new FileSystemResource(file);
                    helper.addAttachment(file.getName(), fsr);
                }
            }

            log.info("Email sent successfully to {}", Arrays.toString(emailMessage.to()));
            mailSender.send(mimeMessage);

    }

    @Override
    public void sendEmailWithTemplate(EmailMessage emailMessage, String templateHtml, Map<String, Object> variables) throws MessagingException {
        try {
            // 1. Thymeleaf Context 생성 및 변수 바인딩
            Context context = new Context();
            context.setVariables(variables);

            // 2. 제목 처리 및 템플릿 렌더링
            String processedSubject = processSubject(emailMessage.subject(), context);
            String processedHtml = templateEngine.process(templateHtml, context);

            // 3. 처리된 내용으로 새로운 EmailMessage 생성 및 발송
            EmailMessage processedEmailMessage = EmailMessage.builder()
                .to(emailMessage.to())
                .subject(processedSubject)
                .content(processedHtml)
                .from(emailMessage.from())
                .attachments(emailMessage.attachments())
                .build();

            sendEmail(processedEmailMessage);

            log.info("Template email sent successfully to {}", Arrays.toString(emailMessage.to()));
        } catch (Exception e) {
            log.error("Failed to send template email to {}: {}", Arrays.toString(emailMessage.to()), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Async("asyncExecutor")
    public void sendEmailWithTemplateAsync(EmailMessage emailMessage, String templateHtml, Map<String, Object> variables) throws MessagingException {
        try {
            // 1. Thymeleaf Context 생성 및 변수 바인딩
            Context context = new Context();
            context.setVariables(variables);

            // 2. 제목 처리 및 템플릿 렌더링
            String processedSubject = processSubject(emailMessage.subject(), context);
            String processedHtml = templateEngine.process(templateHtml, context);

            // 3. 처리된 내용으로 새로운 EmailMessage 생성 및 발송
            EmailMessage processedEmailMessage = EmailMessage.builder()
                .to(emailMessage.to())
                .subject(processedSubject)
                .content(processedHtml)
                .from(emailMessage.from())
                .attachments(emailMessage.attachments())
                .build();

            sendEmail(processedEmailMessage);

            log.info("Template email sent asynchronously to {}", Arrays.toString(emailMessage.to()));
        } catch (Exception e) {
            log.error("Failed to send template email asynchronously to {}: {}", Arrays.toString(emailMessage.to()), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 제목 문자열 내 Thymeleaf 표현식 처리
     */
    private String processSubject(String subject, Context context) {
        if (subject.contains("[[${") || subject.contains("[(${")) {
            try {
                return templateEngine.process(subject, context);
            } catch (Exception e) {
                log.warn("Failed to process subject as template, using as-is: {}", subject);
                return subject;
            }
        }
        return subject;
    }
}
