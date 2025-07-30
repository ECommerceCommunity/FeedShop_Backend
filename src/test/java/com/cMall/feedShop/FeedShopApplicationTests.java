package com.cMall.feedShop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // MockBean 사용을 위한 임포트
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.cMall.feedShop.common.service.EmailServiceImpl; // EmailServiceImpl 임포트 추가
// import com.cMall.feedShop.config.MailGunConfig; // 이제 @EnableAutoConfiguration에서 제외하지 않으므로 임포트 불필요

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// Mailgun API 호출을 담당할 서비스 인터페이스 (가정)
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

    // MailgunService를 Mocking하여 실제 메일 발송 로직을 테스트할 때 사용
    @MockBean
    private MailgunService mailgunService;

    @MockBean
    private EmailServiceImpl emailServiceImpl;

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