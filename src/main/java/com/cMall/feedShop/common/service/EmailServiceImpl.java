// src/main/java/com/cMall/feedShop/common/service/EmailServiceImpl.java
package com.cMall.feedShop.common.service;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final String apiKey;
    private final String domain;
    private final String fromEmail;
    private final MailgunMessagesApi mailgunMessagesApi;


    public EmailServiceImpl(
            @Value("${mailgun.api.key}") String apiKey,
            @Value("${mailgun.domain}") String domain,
            @Value("${mailgun.from.email}") String fromEmail,
            // Spring은 이제 MailgunConfig에서 MailgunMessagesApi 빈을 찾을 것입니다.
            MailgunMessagesApi mailgunMessagesApi) {
        this.apiKey = apiKey;
        this.domain = domain;
        this.fromEmail = fromEmail;
        this.mailgunMessagesApi = mailgunMessagesApi;
        logger.info("EmailServiceImpl 초기화됨: domain: {}", domain);
        logger.info("EmailServiceImpl 초기화됨: apiKey 존재 여부: {}", (apiKey != null && !apiKey.isEmpty()));
    }

    // 환경 변수 검증
    @PostConstruct
    public void validateConfig() {
        Objects.requireNonNull(apiKey, "Mailgun API 키는 null이 아니어야 합니다");
        Objects.requireNonNull(domain, "Mailgun 도메인은 null이 아니어야 합니다");
        Objects.requireNonNull(fromEmail, "Mailgun 보내는 이메일은 null이 아니어야 합니다");
        if (apiKey.isBlank() || domain.isBlank() || fromEmail.isBlank()) {
            throw new IllegalStateException("Mailgun 설정 속성은 비어 있을 수 없습니다");
        }
        logger.info("Mailgun 설정이 EmailServiceImpl에서 성공적으로 검증되었습니다");
    }

    @Override
    public void sendSimpleEmail(String toEmail, String subject, String text) {
        Objects.requireNonNull(toEmail, "받는 이메일은 null이 아니어야 합니다");
        Objects.requireNonNull(subject, "이메일 제목은 null이 아니어야 합니다");
        Objects.requireNonNull(text, "이메일 본문은 null이 아니어야 합니다");

        Message message = Message.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject(subject)
                .text(text)
                .build();

        sendMailgunMessage(message, toEmail);
    }

    @Override
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        Objects.requireNonNull(toEmail, "받는 이메일은 null이 아니어야 합니다");
        Objects.requireNonNull(subject, "이메일 제목은 null이 아니어야 합니다");
        Objects.requireNonNull(htmlContent, "이메일 HTML 내용은 null이 아니어야 합니다");

        Message message = Message.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject(subject)
                .html(htmlContent)
                .build();

        sendMailgunMessage(message, toEmail);
    }

    private void sendMailgunMessage(Message message, String toEmail) {
        try {
            MessageResponse response = mailgunMessagesApi.sendMessage(domain, message);
            logger.info("Mailgun 이메일이 {}로 성공적으로 전송되었습니다: {}", toEmail, response.getMessage());
        } catch (Exception e) {
            logger.error("Mailgun 이메일 {} 전송 실패: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Mailgun 이메일 전송 실패: " + e.getMessage(), e);
        }
    }
}