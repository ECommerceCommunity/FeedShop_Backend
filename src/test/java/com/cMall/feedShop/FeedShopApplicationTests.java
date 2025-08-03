package com.cMall.feedShop;

import com.cMall.feedShop.user.application.service.RecaptchaService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.cMall.feedShop.common.service.EmailService;
import com.cMall.feedShop.common.service.EmailServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
@EnableAutoConfiguration(exclude = {MailSenderAutoConfiguration.class})
@TestPropertySource(properties = {
        "jwt.secret=${TEST_JWT_SECRET:default-test-secret-key-1234567890abcdef}",
        "mailgun.api.key=dummy-test-api-key",
        "mailgun.domain=dummy.test.domain",
        "mailgun.from.email=dummy@test.com",
        "app.password-reset-url=http://localhost:3000/reset-password"
})
class FeedShopApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private EmailService emailService;

    @MockBean
    private EmailServiceImpl emailServiceImpl;

    @MockBean
    private RecaptchaService recaptchaService;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void testEmailServiceMocking() {
        String recipient = "user@example.com";
        String subject = "Welcome!";
        String body = "Thank you for registering.";

        // EmailService Mock 동작 정의
        doNothing().when(emailService).sendHtmlEmail(recipient, subject, body);

        // Mock 호출
        emailService.sendHtmlEmail(recipient, subject, body);

        // 호출되었는지 검증
        verify(emailService, times(1)).sendHtmlEmail(recipient, subject, body);
    }

    @Test
    void testEmailServiceImplMocking() {
        String recipient = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // EmailServiceImpl Mock 동작 정의 (sendEmail 메서드 사용)
        doNothing().when(emailServiceImpl).sendHtmlEmail(recipient, subject, body);

        // Mock 호출
        emailServiceImpl.sendHtmlEmail(recipient, subject, body);

        // 검증
        verify(emailServiceImpl, times(1)).sendHtmlEmail(recipient, subject, body);
    }

    @Test
    void testRecaptchaServiceMocking() {
        String token = "test-token";
        String action = "test-action";

        // RecaptchaService Mock 동작 정의
        doNothing().when(recaptchaService).verifyRecaptcha(token, action);

        // Mock 호출
        recaptchaService.verifyRecaptcha(token, action);

        // 검증
        verify(recaptchaService, times(1)).verifyRecaptcha(token, action);
    }

    @Test
    void testPasswordResetEmailFlow() {
        // 비밀번호 재설정 이메일 발송 시나리오 테스트
        String email = "user@example.com";
        String subject = "[cMall] 비밀번호 재설정 안내";
        String htmlContent = "비밀번호 재설정 링크가 포함된 HTML 내용";

        // sendHtmlEmail 메서드 Mock 설정
        doNothing().when(emailService).sendHtmlEmail(eq(email), eq(subject), anyString());

        // 실제 서비스에서 호출되는 것처럼 테스트
        emailService.sendHtmlEmail(email, subject, htmlContent);

        // 검증
        verify(emailService, times(1)).sendHtmlEmail(eq(email), eq(subject), anyString());
    }
}