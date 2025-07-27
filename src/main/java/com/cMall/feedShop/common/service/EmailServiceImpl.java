package com.cMall.feedShop.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmailAddress;

    @Override
    public void sendSimpleEmail(String toEmail, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmailAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        try {
            emailSender.send(message);
            log.info("이메일 전송 성공: To={}, Subject={}", toEmail, subject);
        } catch (MailException e) {
            log.error("이메일 전송 실패: To={}, Subject={}, Error={}", toEmail, subject, e.getMessage(), e);
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }

    @Override
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmailAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            emailSender.send(message);
            log.info("HTML 이메일 전송 성공: To={}, Subject={}", toEmail, subject);
        } catch (MessagingException e) {
            log.error("HTML 이메일 전송 실패: To={}, Subject={}, Error={}", toEmail, subject, e.getMessage(), e);
            throw new RuntimeException("HTML 이메일 전송에 실패했습니다.", e);
        }
    }
}