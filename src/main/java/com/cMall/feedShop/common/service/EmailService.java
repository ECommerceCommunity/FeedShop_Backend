package com.cMall.feedShop.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender emailSender;

    // 간단한 텍스트 이메일 전송 메서드
    public void sendSimpleEmail(String toEmail, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("jsm1592@gmail.com");
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

    // HTML 형식의 이메일 전송 메서드 (필요하다면 사용)
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            // true는 멀티파트 메시지(파일 첨부 등)를 허용하고, "UTF-8"은 인코딩을 지정합니다.
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("jsm1592@gmail.com"); // 발신자 이메일
            helper.setTo(toEmail); // 수신자 이메일
            helper.setSubject(subject); // 이메일 제목
            helper.setText(htmlContent, true); // true를 넣어 HTML임을 명시

            emailSender.send(message);
            log.info("HTML 이메일 전송 성공: To={}, Subject={}", toEmail, subject);
        } catch (MessagingException e) {
            log.error("HTML 이메일 전송 실패: To={}, Subject={}, Error={}", toEmail, subject, e.getMessage(), e);
            throw new RuntimeException("HTML 이메일 전송에 실패했습니다.", e);
        }
    }
}
