package com.cMall.feedShop.common.email;

public interface EmailService {
    void sendSimpleEmail(String toEmail, String subject, String text);
    void sendHtmlEmail(String toEmail, String subject, String htmlContent);
}