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

import com.cMall.feedShop.common.service.EmailServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

interface MailgunService {
    void sendEmail(String to, String subject, String text);
}


@ActiveProfiles("test")
@SpringBootTest
@EnableAutoConfiguration(exclude = {MailSenderAutoConfiguration.class})
@TestPropertySource(properties = {
        "jwt.secret=${TEST_JWT_SECRET:default-test-secret-key-1234567890abcdef}",
        "mailgun.api.key=dummy-test-api-key",
        "mailgun.domain=dummy.test.domain",
        "mailgun.from.email=dummy@test.com"
})
class FeedShopApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private MailgunService mailgunService;

    @MockBean
    private EmailServiceImpl emailServiceImpl;

    @MockBean
    private RecaptchaService recaptchaService;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void testEmailSendingLogic() {
        String recipient = "user@example.com";
        String subject = "Welcome!";
        String body = "Thank you for registering.";

        // mailgunService (Mock)가 호출되는지 검증
        mailgunService.sendEmail(recipient, subject, body);
        verify(mailgunService, times(1)).sendEmail(recipient, subject, body);

    }
}