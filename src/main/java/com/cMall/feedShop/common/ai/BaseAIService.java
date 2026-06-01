package com.cMall.feedShop.common.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI 서비스 비활성화 버전 (Spring AI 의존성 없이 동작)
 * Spring AI 의존성 제거 시 이 구현체가 사용됩니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BaseAIService {
    private final ObjectMapper objectMapper;

    private final String fallbackText = "{\"message\": \"AI 서비스가 현재 사용 불가능합니다.\"}";

    public String generateText(String prompt) {
        log.info("AI 비활성화 상태 - 폴백 반환");
        return fallbackText;
    }

    public <T extends BaseAIResponse<?>> T parseAIResponse(String response, Class<T> responseClass) {
        try {
            return responseClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("빈 응답 객체 생성 실패: {}", responseClass.getSimpleName(), e);
            throw new RuntimeException("기본 응답 객체 생성 실패", e);
        }
    }
}
