package com.cMall.feedShop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class FeedShopApplicationTests {

    @Test
    void contextLoads() {
        // 단순히 컨텍스트가 로드되는지만 확인
        assertThat(true).isTrue();
    }
}