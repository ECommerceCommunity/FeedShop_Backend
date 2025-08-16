package com.cMall.feedShop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableJpaAuditing
@EnableScheduling
public class FeedShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedShopApplication.class, args);
	}

}

