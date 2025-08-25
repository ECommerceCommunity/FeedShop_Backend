package com.cMall.feedShop.common.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaseAIService {
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Value("${ai.enabled:true}")
    private boolean aiEnabled;

    private final String fallbackText = "{\"message\": \"AI 서비스가 현재 사용 불가능합니다.\"}";

    // AI 모델을 사용하여 텍스트 생성, 실패 시 폴백 텍스트 반환
    public String generateText(String prompt) {
        if (!aiEnabled || chatModel == null) {
            log.info("AI 비활성화 상태 (aiEnabled: {}, chatModel: {}), 폴백 반환",
                    aiEnabled, chatModel != null ? "있음" : "없음");
            return fallbackText;
        }

        try {
            ChatResponse response = chatModel.call(new Prompt(prompt));
            String result = response.getResult().getOutput().getContent();
            if (result == null || result.isBlank()) {
                return fallbackText;
            }
            log.debug("AI 텍스트 생성 성공");
            return result;
        } catch (Exception e) {
            log.warn("AI 텍스트 생성 실패: {}", e.getMessage());
            return fallbackText;
        }
    }

    // AI 응답을 BaseAIResponse 객체로 변환
    public <T extends BaseAIResponse<?>> T parseAIResponse(String response, Class<T> responseClass) {
        try {
            if (response == null || response.trim().isEmpty()) {
                log.warn("AI 응답이 비어있음");
                return createEmptyResponse(responseClass);
            }

            String cleanJson = cleanJsonResponse(response);
            if (cleanJson.isBlank()) {
                log.warn("정리된 JSON이 비어있음");
                return createEmptyResponse(responseClass);
            }

            T result = objectMapper.readValue(cleanJson, responseClass);
            if (result == null) {
                log.warn("파싱 결과가 null");
                return createEmptyResponse(responseClass);
            }

            log.debug("AI 응답 파싱 성공: {}", responseClass.getSimpleName());
            return result;

        } catch (Exception e) {
            log.warn("AI 응답 파싱 실패 ({}): {}", responseClass.getSimpleName(), e.getMessage());
            return createEmptyResponse(responseClass);
        }
    }

    private <T extends BaseAIResponse<?>> T createEmptyResponse(Class<T> responseClass) {
        try {
            return responseClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("빈 응답 객체 생성 실패: {}", responseClass.getSimpleName(), e);
            throw new RuntimeException("기본 응답 객체 생성 실패", e);
        }
    }

    // ```json ... ``` 또는 불필요한 전후 텍스트 제거 + 첫 { ~ 마지막 } 까지 슬라이싱
    private String cleanJsonResponse(String response) {
        String s = response == null ? "" : response.trim();
        // 코드펜스 제거
        if (s.startsWith("```")) {
            int firstNewline = s.indexOf('\n');
            int lastFence = s.lastIndexOf("```");
            if (firstNewline >= 0 && lastFence > firstNewline) {
                s = s.substring(firstNewline + 1, lastFence).trim();
            }
        }
        // 첫 '{'와 마지막 '}' 사이만 추출
        Pattern pattern = Pattern.compile("\\{.*?\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return "";
    }
}
