package com.cMall.feedShop;

import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.common.email.EmailService;
import com.cMall.feedShop.common.storage.GcpStorageService;
import com.cMall.feedShop.common.validator.ImageValidator;
import com.cMall.feedShop.common.captcha.RecaptchaVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class FeedShopApplicationTests {

    @MockBean
    private StorageService storageService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private GcpStorageService gcpStorageService;

    @MockBean
    private ImageValidator imageValidator;

    @MockBean
    private RecaptchaVerificationService recaptchaVerificationService;

    @Test
    void contextLoads() {
        // 단순히 컨텍스트가 로드되는지만 확인
        assertThat(true).isTrue();
    }

    @Test
    void testRecaptchaServiceMocking() {
        String token = "test-token";
        String action = "test-action";

        // Mock 설정 - void 메서드이므로 doNothing 사용
        doNothing().when(recaptchaVerificationService).verifyRecaptcha(token, action);

        // 검증 - 예외가 발생하지 않으면 성공
        assertThatCode(() -> {
            recaptchaVerificationService.verifyRecaptcha(token, action);
        }).doesNotThrowAnyException();
        
        verify(recaptchaVerificationService).verifyRecaptcha(token, action);
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