package com.cMall.feedShop.config;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class MailGunConfig {

    private static final Logger logger = LoggerFactory.getLogger(MailGunConfig.class);

    @Value("${mailgun.api.key}") // application.properties 또는 application.yml에서 API 키를 주입합니다.
    private String apiKey;

    @Bean // 이 메서드가 Spring 빈을 생성하고 관리함을 나타냅니다.
    public MailgunMessagesApi mailgunMessagesApi() {
        logger.info("MailgunConfig에서 MailgunMessagesApi 빈을 API 키로 생성 중.");
        return MailgunClient.config(apiKey) // 주입된 apiKey로 MailgunClient를 설정합니다.
                .createApi(MailgunMessagesApi.class);
    }

}
