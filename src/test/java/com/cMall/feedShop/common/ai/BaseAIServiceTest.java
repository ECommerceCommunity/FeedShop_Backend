package com.cMall.feedShop.common.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaseAIService 테스트")
class BaseAIServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BaseAIService baseAIService;

    private TestAIResponse testResponse;

    @BeforeEach
    void setUp() {
        testResponse = new TestAIResponse();
        ReflectionTestUtils.setField(testResponse, "status", "OK");
        ReflectionTestUtils.setField(testResponse, "message", "테스트 성공");
        ReflectionTestUtils.setField(testResponse, "data", "테스트 데이터");
    }

    @Test
    @DisplayName("AI 활성화 상태에서 텍스트 생성 성공")
    void generateText_WhenAIEnabled_Success() {
        // given
        ReflectionTestUtils.setField(baseAIService, "aiEnabled", true);
        String prompt = "테스트 프롬프트";
        String expectedResponse = "AI 응답";

        ChatResponse mockResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        AssistantMessage mockMessage = mock(AssistantMessage.class);

        given(chatModel.call(any(Prompt.class))).willReturn(mockResponse);
        given(mockResponse.getResult()).willReturn(mockGeneration);
        given(mockGeneration.getOutput()).willReturn(mockMessage);
        given(mockMessage.getContent()).willReturn(expectedResponse);

        // when
        String result = baseAIService.generateText(prompt);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    @DisplayName("AI 비활성화 상태에서 폴백 텍스트 반환")
    void generateText_WhenAIDisabled_ReturnsFallback() {
        // given
        ReflectionTestUtils.setField(baseAIService, "aiEnabled", false);
        String prompt = "테스트 프롬프트";

        // when
        String result = baseAIService.generateText(prompt);

        // then
        assertThat(result).isEqualTo("{\"message\": \"AI 서비스가 현재 사용 불가능합니다.\"}");
        verify(chatModel, never()).call(any(Prompt.class));
    }

    @Test
    @DisplayName("ChatModel이 null일 때 폴백 텍스트 반환")
    void generateText_WhenChatModelIsNull_ReturnsFallback() {
        // given
        ReflectionTestUtils.setField(baseAIService, "aiEnabled", true);
        ReflectionTestUtils.setField(baseAIService, "chatModel", null);
        String prompt = "테스트 프롬프트";

        // when
        String result = baseAIService.generateText(prompt);

        // then
        assertThat(result).isEqualTo("{\"message\": \"AI 서비스가 현재 사용 불가능합니다.\"}");
    }

    @Test
    @DisplayName("AI 응답이 비어있을 때 폴백 텍스트 반환")
    void generateText_WhenResponseIsEmpty_ReturnsFallback() {
        // given
        ReflectionTestUtils.setField(baseAIService, "aiEnabled", true);
        String prompt = "테스트 프롬프트";

        ChatResponse mockResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        AssistantMessage mockMessage = mock(AssistantMessage.class);

        given(chatModel.call(any(Prompt.class))).willReturn(mockResponse);
        given(mockResponse.getResult()).willReturn(mockGeneration);
        given(mockGeneration.getOutput()).willReturn(mockMessage);
        given(mockMessage.getContent()).willReturn("");

        // when
        String result = baseAIService.generateText(prompt);

        // then
        assertThat(result).isEqualTo("{\"message\": \"AI 서비스가 현재 사용 불가능합니다.\"}");
    }

    @Test
    @DisplayName("AI 호출 중 예외 발생 시 폴백 텍스트 반환")
    void generateText_WhenExceptionOccurs_ReturnsFallback() {
        // given
        ReflectionTestUtils.setField(baseAIService, "aiEnabled", true);
        String prompt = "테스트 프롬프트";

        given(chatModel.call(any(Prompt.class))).willThrow(new RuntimeException("AI 서비스 오류"));

        // when
        String result = baseAIService.generateText(prompt);

        // then
        assertThat(result).isEqualTo("{\"message\": \"AI 서비스가 현재 사용 불가능합니다.\"}");
    }

    @Test
    @DisplayName("정상적인 JSON 응답 파싱 성공")
    void parseAIResponse_ValidJson_Success() throws JsonProcessingException {
        // given
        String jsonResponse = "{\"status\":\"OK\",\"message\":\"성공\",\"data\":\"테스트 데이터\"}";
        
        given(objectMapper.readValue(jsonResponse, TestAIResponse.class))
                .willReturn(testResponse);

        // when
        TestAIResponse result = baseAIService.parseAIResponse(jsonResponse, TestAIResponse.class);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        verify(objectMapper).readValue(jsonResponse, TestAIResponse.class);
    }

    @Test
    @DisplayName("코드펜스로 감싸진 JSON 응답 파싱 성공")
    void parseAIResponse_WithCodeFence_Success() throws JsonProcessingException {
        // given
        String jsonResponse = "```json\n{\"status\":\"OK\",\"message\":\"성공\",\"data\":\"테스트 데이터\"}\n```";
        String cleanJson = "{\"status\":\"OK\",\"message\":\"성공\",\"data\":\"테스트 데이터\"}";
        
        given(objectMapper.readValue(cleanJson, TestAIResponse.class))
                .willReturn(testResponse);

        // when
        TestAIResponse result = baseAIService.parseAIResponse(jsonResponse, TestAIResponse.class);

        // then
        assertThat(result).isNotNull();
        verify(objectMapper).readValue(cleanJson, TestAIResponse.class);
    }

    @Test
    @DisplayName("불필요한 텍스트가 포함된 JSON 응답 파싱 성공")
    void parseAIResponse_WithExtraText_Success() throws JsonProcessingException {
        // given
        String jsonResponse = "여기는 설명입니다. {\"status\":\"OK\",\"message\":\"성공\",\"data\":\"테스트 데이터\"} 추가 설명";
        String cleanJson = "{\"status\":\"OK\",\"message\":\"성공\",\"data\":\"테스트 데이터\"}";
        
        given(objectMapper.readValue(cleanJson, TestAIResponse.class))
                .willReturn(testResponse);

        // when
        TestAIResponse result = baseAIService.parseAIResponse(jsonResponse, TestAIResponse.class);

        // then
        assertThat(result).isNotNull();
        verify(objectMapper).readValue(cleanJson, TestAIResponse.class);
    }

    @Test
    @DisplayName("빈 응답일 때 빈 응답 객체 반환")
    void parseAIResponse_EmptyResponse_ReturnsEmptyObject() throws JsonProcessingException {
        // given
        String emptyResponse = "";

        // when
        TestAIResponse result = baseAIService.parseAIResponse(emptyResponse, TestAIResponse.class);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        verify(objectMapper, never()).readValue(anyString(), eq(TestAIResponse.class));
    }

    @Test
    @DisplayName("null 응답일 때 빈 응답 객체 반환")
    void parseAIResponse_NullResponse_ReturnsEmptyObject() throws JsonProcessingException {
        // given
        String nullResponse = null;

        // when
        TestAIResponse result = baseAIService.parseAIResponse(nullResponse, TestAIResponse.class);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        verify(objectMapper, never()).readValue(anyString(), eq(TestAIResponse.class));
    }

    @Test
    @DisplayName("JSON 파싱 실패 시 빈 응답 객체 반환")
    void parseAIResponse_ParseFailure_ReturnsEmptyObject() throws JsonProcessingException {
        // given
        String invalidJson = "{\"invalid\":}";
        
        given(objectMapper.readValue(anyString(), eq(TestAIResponse.class)))
                .willThrow(new RuntimeException("파싱 오류"));

        // when
        TestAIResponse result = baseAIService.parseAIResponse(invalidJson, TestAIResponse.class);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("응답 클래스 인스턴스 생성 실패 시 예외 발생")
    void parseAIResponse_ConstructorFailure_ThrowsException() throws JsonProcessingException {
        // given
        String jsonResponse = "{\"status\":\"OK\"}";
        
        given(objectMapper.readValue(anyString(), eq(InvalidResponse.class)))
                .willThrow(new RuntimeException("파싱 오류"));

        // when & then
        assertThatThrownBy(() -> baseAIService.parseAIResponse(jsonResponse, InvalidResponse.class))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("기본 응답 객체 생성 실패");
    }

    // 테스트용 응답 클래스
    static class TestAIResponse extends BaseAIResponse<String> {
        public TestAIResponse() {
            super();
        }
    }

    // 생성자가 없는 테스트용 클래스 (인스턴스 생성 실패 테스트용)
    static class InvalidResponse extends BaseAIResponse<String> {
        private InvalidResponse(String param) {
            // private 생성자로 인한 인스턴스 생성 실패 시뮬레이션
        }
    }
}