package com.cMall.feedShop.common.service;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private MailgunMessagesApi mailgunMessagesApi;

    private EmailServiceImpl emailService;

    private final String apiKey = "test-api-key";
    private final String domain = "test-domain.com";
    private final String fromEmail = "sender@test-domain.com";

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(apiKey, domain, fromEmail, mailgunMessagesApi);
    }

    @Test
    @DisplayName("단순 텍스트 이메일 전송 - 성공")
    void sendSimpleEmail_success() throws Exception {
        // given
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Hello, this is a test email.";

        MessageResponse mockResponse = mock(MessageResponse.class);
        when(mockResponse.getMessage()).thenReturn("Queued. Thank you.");
        when(mailgunMessagesApi.sendMessage(anyString(), any(Message.class))).thenReturn(mockResponse);

        // when
        emailService.sendSimpleEmail(toEmail, subject, text);

        // then
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(mailgunMessagesApi, times(1)).sendMessage(eq(domain), messageCaptor.capture());

        Message capturedMessage = messageCaptor.getValue();
        assertEquals(fromEmail, capturedMessage.getFrom());
        assertTrue(capturedMessage.getTo().contains(toEmail));
        assertEquals(subject, capturedMessage.getSubject());
        assertEquals(text, capturedMessage.getText());
    }

    @Test
    @DisplayName("HTML 이메일 전송 - 성공")
    void sendHtmlEmail_success() throws Exception {
        // given
        String toEmail = "recipient@example.com";
        String subject = "HTML Test Subject";
        String htmlContent = "<h1>Hello</h1><p>This is an HTML email.</p>";

        MessageResponse mockResponse = mock(MessageResponse.class);
        when(mockResponse.getMessage()).thenReturn("Queued. Thank you.");
        when(mailgunMessagesApi.sendMessage(anyString(), any(Message.class))).thenReturn(mockResponse);

        // when
        emailService.sendHtmlEmail(toEmail, subject, htmlContent);

        // then
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(mailgunMessagesApi, times(1)).sendMessage(eq(domain), messageCaptor.capture());

        Message capturedMessage = messageCaptor.getValue();
        assertEquals(fromEmail, capturedMessage.getFrom());
        assertTrue(capturedMessage.getTo().contains(toEmail));
        assertEquals(subject, capturedMessage.getSubject());
        assertEquals(htmlContent, capturedMessage.getHtml());
    }

    @Test
    @DisplayName("이메일 전송 - 실패 (Mailgun API 예외 발생)")
    void sendEmail_fail_mailgunException() throws Exception {
        // given
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Hello, this is a test email.";

        when(mailgunMessagesApi.sendMessage(anyString(), any(Message.class)))
                .thenThrow(new RuntimeException("Mailgun API error"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendSimpleEmail(toEmail, subject, text);
        });

        assertTrue(exception.getMessage().contains("Mailgun 이메일 전송 실패"));
    }

    @Test
    @DisplayName("생성자 - 설정 값 검증 성공")
    void constructor_validation_success() {
        // when & then
        assertDoesNotThrow(() -> {
            emailService.validateConfig();
        });
    }

    @Test
    @DisplayName("생성자 - 설정 값 검증 실패 (API 키 없음)")
    void constructor_validation_fail_noApiKey() {
        // given
        emailService = new EmailServiceImpl(null, domain, fromEmail, mailgunMessagesApi);

        // when & then
        assertThrows(NullPointerException.class, () -> {
            emailService.validateConfig();
        });
    }
}
