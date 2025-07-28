package com.cMall.feedShop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@SpringBootTest
@EnableAutoConfiguration(exclude = {MailSenderAutoConfiguration.class})
@TestPropertySource(properties = {
        "jwt.secret=${TEST_JWT_SECRET:default-test-secret-key-1234567890abcdef}"})
class FeedShopApplicationTests {

    @MockBean
    private JavaMailSender javaMailSender;
    @Test
    void contextLoads() {
    }
} 