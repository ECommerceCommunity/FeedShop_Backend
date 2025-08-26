package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
import com.cMall.feedShop.feed.application.service.FeedCreateService;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedCreateController 테스트")
class FeedCreateControllerTest {

    @Mock
    private FeedCreateService feedCreateService;

    @InjectMocks
    private FeedCreateController feedCreateController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    private FeedCreateRequestDto testRequestDto;
    private FeedCreateResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(feedCreateController)
                
                .build();
        objectMapper = new ObjectMapper();

        // 테스트용 사용자 생성
        testUser = User.builder()
                .loginId("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();



        // 테스트용 요청 DTO 생성
        testRequestDto = FeedCreateRequestDto.builder()
                .title("테스트 피드")
                .content("테스트 피드 내용입니다.")
                .instagramId("test_instagram")
                .hashtags(Arrays.asList("테스트", "피드"))
                .orderItemId(1L)
                .eventId(null)
                .build();

        // 테스트용 응답 DTO 생성
        testResponseDto = FeedCreateResponseDto.builder()
                .feedId(1L)
                .title("테스트 피드")
                .content("테스트 피드 내용입니다.")
                .build();
    }

    @Test
    @DisplayName("이미지와 함께 피드 생성 - 성공")
    void createFeedWithImages_Success() throws Exception {
        // given
        given(feedCreateService.createFeedWithImages(any(FeedCreateRequestDto.class), any(), eq("testuser")))
                .willReturn(testResponseDto);

        MockMultipartFile feedData = new MockMultipartFile(
                "feedData",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(testRequestDto)
        );

        MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "test1.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        MockMultipartFile image2 = new MockMultipartFile(
                "images",
                "test2.jpg",
                "image/jpeg",
                "test image content 2".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/feeds")
                        .file(feedData)
                        .file(image1)
                        .file(image2)
                        .param("username", "testuser")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(1L))
                .andExpect(jsonPath("$.data.title").value("테스트 피드"))
                .andExpect(jsonPath("$.data.content").value("테스트 피드 내용입니다."));

        verify(feedCreateService).createFeedWithImages(any(FeedCreateRequestDto.class), any(), eq("testuser"));
    }

    @Test
    @DisplayName("이미지 없이 피드 생성 - 성공")
    void createFeedWithImages_NoImages_Success() throws Exception {
        // given
        given(feedCreateService.createFeedWithImages(any(FeedCreateRequestDto.class), any(), eq("testuser")))
                .willReturn(testResponseDto);

        MockMultipartFile feedData = new MockMultipartFile(
                "feedData",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(testRequestDto)
        );

        // when & then
        mockMvc.perform(multipart("/api/feeds")
                        .file(feedData)
                        .param("username", "testuser")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(1L));

        verify(feedCreateService).createFeedWithImages(any(FeedCreateRequestDto.class), any(), eq("testuser"));
    }

    @Test
    @DisplayName("텍스트만으로 피드 생성 - 성공")
    void createFeed_TextOnly_Success() throws Exception {
        // given
        given(feedCreateService.createFeed(any(FeedCreateRequestDto.class), eq("testuser")))
                .willReturn(testResponseDto);

        // when & then
        mockMvc.perform(post("/api/feeds/text-only")
                        .param("username", "testuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(1L))
                .andExpect(jsonPath("$.data.title").value("테스트 피드"));

        verify(feedCreateService).createFeed(any(FeedCreateRequestDto.class), eq("testuser"));
    }



    @Test
    @DisplayName("피드 생성 - 잘못된 요청 데이터")
    void createFeed_InvalidRequest() throws Exception {
        // given
        FeedCreateRequestDto invalidRequest = FeedCreateRequestDto.builder()
                .title("") // 빈 제목
                .content("테스트 내용")
                .build();

        // when & then
        mockMvc.perform(post("/api/feeds/text-only")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이벤트 피드 생성 - 성공")
    void createFeed_EventFeed_Success() throws Exception {
        // given
        FeedCreateRequestDto eventRequest = FeedCreateRequestDto.builder()
                .title("이벤트 피드")
                .content("이벤트 피드 내용")
                .instagramId("event_instagram")
                .hashtags(Arrays.asList("이벤트", "참여"))
                .orderItemId(1L)
                .eventId(1L)
                .build();

        FeedCreateResponseDto eventResponse = FeedCreateResponseDto.builder()
                .feedId(2L)
                .title("이벤트 피드")
                .content("이벤트 피드 내용")
                .build();

        given(feedCreateService.createFeed(any(FeedCreateRequestDto.class), eq("testuser")))
                .willReturn(eventResponse);

        // when & then
        mockMvc.perform(post("/api/feeds/text-only")
                        .param("username", "testuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(2L))
                .andExpect(jsonPath("$.data.title").value("이벤트 피드"));

        verify(feedCreateService).createFeed(any(FeedCreateRequestDto.class), eq("testuser"));
    }

    @Test
    @DisplayName("해시태그가 있는 피드 생성 - 성공")
    void createFeed_WithHashtags_Success() throws Exception {
        // given
        FeedCreateRequestDto hashtagRequest = FeedCreateRequestDto.builder()
                .title("해시태그 피드")
                .content("해시태그가 포함된 피드")
                .instagramId("hashtag_instagram")
                .hashtags(Arrays.asList("해시태그1", "해시태그2", "해시태그3"))
                .orderItemId(1L)
                .eventId(null)
                .build();

        given(feedCreateService.createFeed(any(FeedCreateRequestDto.class), eq("testuser")))
                .willReturn(testResponseDto);

        // when & then
        mockMvc.perform(post("/api/feeds/text-only")
                        .param("username", "testuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hashtagRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedCreateService).createFeed(any(FeedCreateRequestDto.class), eq("testuser"));
    }
}
