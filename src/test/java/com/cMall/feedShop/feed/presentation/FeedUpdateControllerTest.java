package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.feed.application.dto.request.FeedUpdateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.service.FeedUpdateService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedUpdateController 테스트")
class FeedUpdateControllerTest {

    @Mock
    private FeedUpdateService feedUpdateService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private FeedUpdateController feedUpdateController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    private FeedUpdateRequestDto testRequestDto;
    private FeedDetailResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(feedUpdateController)
                
                .build();
        objectMapper = new ObjectMapper();

        // 테스트용 사용자 생성
        testUser = User.builder()
                .loginId("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        // UserDetails 모킹 설정
        org.mockito.BDDMockito.given(userDetails.getUsername()).willReturn("testuser");

        // 테스트용 요청 DTO 생성
        testRequestDto = FeedUpdateRequestDto.builder()
                .title("수정된 피드 제목")
                .content("수정된 피드 내용입니다.")
                .instagramId("updated_instagram")
                .hashtags(Arrays.asList("수정", "피드"))
                .build();

        // 테스트용 응답 DTO 생성
        testResponseDto = FeedDetailResponseDto.builder()
                .feedId(1L)
                .title("수정된 피드 제목")
                .content("수정된 피드 내용입니다.")
                .userNickname("테스트 사용자")
                .likeCount(10)
                .commentCount(5)
                .participantVoteCount(3)
                .createdAt(LocalDateTime.now())
                .feedType(FeedType.DAILY)
                .isLiked(false)
                .isVoted(false)
                .build();
    }

    @Test
    @DisplayName("이미지와 함께 피드 수정 - 성공")
    void updateFeedWithImages_Success() throws Exception {
        // given
        given(feedUpdateService.updateFeedWithImages(eq(1L), any(FeedUpdateRequestDto.class), any(), eq(userDetails)))
                .willReturn(testResponseDto);

        MockMultipartFile feedData = new MockMultipartFile(
                "feedData",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(testRequestDto)
        );

        MockMultipartFile newImage = new MockMultipartFile(
                "newImages",
                "new-image.jpg",
                "image/jpeg",
                "new image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/feeds/1")
                        .file(feedData)
                        .file(newImage)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(1L))
                .andExpect(jsonPath("$.data.title").value("수정된 피드 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 피드 내용입니다."));

        verify(feedUpdateService).updateFeedWithImages(eq(1L), any(FeedUpdateRequestDto.class), any(), eq(userDetails));
    }

    @Test
    @DisplayName("이미지 없이 피드 수정 - 성공")
    void updateFeedWithImages_NoImages_Success() throws Exception {
        // given
        given(feedUpdateService.updateFeedWithImages(eq(1L), any(FeedUpdateRequestDto.class), any(), eq(userDetails)))
                .willReturn(testResponseDto);

        MockMultipartFile feedData = new MockMultipartFile(
                "feedData",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(testRequestDto)
        );

        // when & then
        mockMvc.perform(multipart("/api/feeds/1")
                        .file(feedData)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(1L));

        verify(feedUpdateService).updateFeedWithImages(eq(1L), any(FeedUpdateRequestDto.class), any(), eq(userDetails));
    }

    @Test
    @DisplayName("텍스트만으로 피드 수정 - 성공")
    void updateFeed_TextOnly_Success() throws Exception {
        // given
        given(feedUpdateService.updateFeed(eq(1L), any(FeedUpdateRequestDto.class), eq(userDetails)))
                .willReturn(testResponseDto);

        // when & then
        mockMvc.perform(put("/api/feeds/1/text-only")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(1L))
                .andExpect(jsonPath("$.data.title").value("수정된 피드 제목"));

        verify(feedUpdateService).updateFeed(eq(1L), any(FeedUpdateRequestDto.class), eq(userDetails));
    }

    @Test
    @DisplayName("피드 수정 - 인증되지 않은 사용자")
    void updateFeed_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(put("/api/feeds/1/text-only")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("피드 수정 - 잘못된 요청 데이터")
    void updateFeed_InvalidRequest() throws Exception {
        // given
        FeedUpdateRequestDto invalidRequest = FeedUpdateRequestDto.builder()
                .title("") // 빈 제목
                .content("테스트 내용")
                .build();

        // when & then
        mockMvc.perform(put("/api/feeds/1/text-only")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("해시태그 수정 - 성공")
    void updateFeed_WithHashtags_Success() throws Exception {
        // given
        FeedUpdateRequestDto hashtagRequest = FeedUpdateRequestDto.builder()
                .title("해시태그 수정 피드")
                .content("해시태그가 수정된 피드")
                .instagramId("hashtag_instagram")
                .hashtags(Arrays.asList("새해시태그1", "새해시태그2"))
                .build();

        given(feedUpdateService.updateFeed(eq(1L), any(FeedUpdateRequestDto.class), eq(userDetails)))
                .willReturn(testResponseDto);

        // when & then
        mockMvc.perform(put("/api/feeds/1/text-only")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hashtagRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedUpdateService).updateFeed(eq(1L), any(FeedUpdateRequestDto.class), eq(userDetails));
    }

    @Test
    @DisplayName("이미지 삭제와 함께 피드 수정 - 성공")
    void updateFeed_WithImageDeletion_Success() throws Exception {
        // given
        FeedUpdateRequestDto imageDeletionRequest = FeedUpdateRequestDto.builder()
                .title("이미지 삭제 피드")
                .content("이미지가 삭제된 피드")
                .instagramId("image_deletion_instagram")
                .hashtags(Arrays.asList("이미지", "삭제"))
                .build();

        given(feedUpdateService.updateFeedWithImages(eq(1L), any(FeedUpdateRequestDto.class), any(), eq(userDetails)))
                .willReturn(testResponseDto);

        MockMultipartFile feedData = new MockMultipartFile(
                "feedData",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(imageDeletionRequest)
        );

        // when & then
        mockMvc.perform(multipart("/api/feeds/1")
                        .file(feedData)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedUpdateService).updateFeedWithImages(eq(1L), any(FeedUpdateRequestDto.class), any(), eq(userDetails));
    }

    @Test
    @DisplayName("빈 해시태그로 피드 수정 - 성공")
    void updateFeed_EmptyHashtags_Success() throws Exception {
        // given
        FeedUpdateRequestDto emptyHashtagRequest = FeedUpdateRequestDto.builder()
                .title("빈 해시태그 피드")
                .content("해시태그가 없는 피드")
                .instagramId("empty_hashtag_instagram")
                .hashtags(Arrays.asList())
                .build();

        given(feedUpdateService.updateFeed(eq(1L), any(FeedUpdateRequestDto.class), eq(userDetails)))
                .willReturn(testResponseDto);

        // when & then
        mockMvc.perform(put("/api/feeds/1/text-only")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyHashtagRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedUpdateService).updateFeed(eq(1L), any(FeedUpdateRequestDto.class), eq(userDetails));
    }

    @Test
    @DisplayName("다른 피드 ID로 수정 - 성공")
    void updateFeed_DifferentFeedId_Success() throws Exception {
        // given
        given(feedUpdateService.updateFeed(eq(999L), any(FeedUpdateRequestDto.class), eq(userDetails)))
                .willReturn(testResponseDto);

        // when & then
        mockMvc.perform(put("/api/feeds/999/text-only")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedUpdateService).updateFeed(eq(999L), any(FeedUpdateRequestDto.class), eq(userDetails));
    }
}
