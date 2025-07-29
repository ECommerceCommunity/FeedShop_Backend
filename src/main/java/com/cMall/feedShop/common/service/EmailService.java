package com.cMall.feedShop.common.service;

public interface EmailService {
    void sendSimpleEmail(String toEmail, String subject, String text);
    void sendHtmlEmail(String toEmail, String subject, String htmlContent);
}