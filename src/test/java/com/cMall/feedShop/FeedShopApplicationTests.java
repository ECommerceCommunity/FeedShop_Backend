package com.cMall.feedShop;

import com.cMall.feedShop.common.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class FeedShopApplicationTests {

    @MockBean
    private StorageService storageService;

    @Test
    void contextLoads() {
        // 단순히 컨텍스트가 로드되는지만 확인
        assertThat(true).isTrue();
    }
}