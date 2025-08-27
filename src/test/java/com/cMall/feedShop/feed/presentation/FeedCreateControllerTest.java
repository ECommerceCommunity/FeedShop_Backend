package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
import com.cMall.feedShop.feed.application.service.FeedCreateService;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedCreateController 테스트")
class FeedCreateControllerTest {

    @Mock
    private FeedCreateService feedCreateService;

    @InjectMocks
    private FeedCreateController feedCreateController;

    private ObjectMapper objectMapper;
    private User testUser;
    private UserDetails testUserDetails;
    private FeedCreateRequestDto testRequestDto;
    private FeedCreateResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // 테스트용 사용자 생성
        testUser = User.builder()
                .loginId("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        // 테스트용 UserDetails 생성
        testUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
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
    @DisplayName("피드 생성 (이미지 포함) - 성공")
    void createFeedWithImages_Success() {
        // given
        List<MockMultipartFile> mockImages = Arrays.asList(
                new MockMultipartFile("images", "test1.jpg", "image/jpeg", "test image 1".getBytes()),
                new MockMultipartFile("images", "test2.jpg", "image/jpeg", "test image 2".getBytes())
        );
        @SuppressWarnings("unchecked")
        List<org.springframework.web.multipart.MultipartFile> images = (List<org.springframework.web.multipart.MultipartFile>) (List<?>) mockImages;

        given(feedCreateService.createFeedWithImages(any(FeedCreateRequestDto.class), any(), eq("testuser")))
                .willReturn(testResponseDto);

        // when
        ApiResponse<FeedCreateResponseDto> response = feedCreateController.createFeedWithImages(
                testRequestDto, images, testUserDetails);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getFeedId()).isEqualTo(1L);
        assertThat(response.getData().getTitle()).isEqualTo("테스트 피드");
        assertThat(response.getData().getContent()).isEqualTo("테스트 피드 내용입니다.");

        verify(feedCreateService).createFeedWithImages(any(FeedCreateRequestDto.class), any(), eq("testuser"));
    }

    @Test
    @DisplayName("피드 생성 (이미지 없음) - 성공")
    void createFeed_TextOnly_Success() {
        // given
        given(feedCreateService.createFeed(any(FeedCreateRequestDto.class), eq("testuser")))
                .willReturn(testResponseDto);

        // when
        ApiResponse<FeedCreateResponseDto> response = feedCreateController.createFeed(
                testRequestDto, testUserDetails);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getFeedId()).isEqualTo(1L);
        assertThat(response.getData().getTitle()).isEqualTo("테스트 피드");
        assertThat(response.getData().getContent()).isEqualTo("테스트 피드 내용입니다.");

        verify(feedCreateService).createFeed(any(FeedCreateRequestDto.class), eq("testuser"));
    }

    @Test
    @DisplayName("이벤트 피드 생성 (이미지 포함) - 성공")
    void createFeedWithImages_EventFeed_Success() {
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

        List<MockMultipartFile> mockImages = Arrays.asList(
                new MockMultipartFile("images", "event.jpg", "image/jpeg", "event image".getBytes())
        );
        @SuppressWarnings("unchecked")
        List<org.springframework.web.multipart.MultipartFile> images = (List<org.springframework.web.multipart.MultipartFile>) (List<?>) mockImages;

        given(feedCreateService.createFeedWithImages(any(FeedCreateRequestDto.class), any(), eq("testuser")))
                .willReturn(eventResponse);

        // when
        ApiResponse<FeedCreateResponseDto> response = feedCreateController.createFeedWithImages(
                eventRequest, images, testUserDetails);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getFeedId()).isEqualTo(2L);
        assertThat(response.getData().getTitle()).isEqualTo("이벤트 피드");

        verify(feedCreateService).createFeedWithImages(any(FeedCreateRequestDto.class), any(), eq("testuser"));
    }

    @Test
    @DisplayName("해시태그가 있는 피드 생성 (이미지 포함) - 성공")
    void createFeedWithImages_WithHashtags_Success() {
        // given
        FeedCreateRequestDto hashtagRequest = FeedCreateRequestDto.builder()
                .title("해시태그 피드")
                .content("해시태그가 포함된 피드")
                .instagramId("hashtag_instagram")
                .hashtags(Arrays.asList("해시태그1", "해시태그2", "해시태그3"))
                .orderItemId(1L)
                .eventId(null)
                .build();

        List<MockMultipartFile> mockImages = Arrays.asList(
                new MockMultipartFile("images", "hashtag.jpg", "image/jpeg", "hashtag image".getBytes())
        );
        @SuppressWarnings("unchecked")
        List<org.springframework.web.multipart.MultipartFile> images = (List<org.springframework.web.multipart.MultipartFile>) (List<?>) mockImages;

        given(feedCreateService.createFeedWithImages(any(FeedCreateRequestDto.class), any(), eq("testuser")))
                .willReturn(testResponseDto);

        // when
        ApiResponse<FeedCreateResponseDto> response = feedCreateController.createFeedWithImages(
                hashtagRequest, images, testUserDetails);

        // then
        assertThat(response.isSuccess()).isTrue();

        verify(feedCreateService).createFeedWithImages(any(FeedCreateRequestDto.class), any(), eq("testuser"));
    }

    @Test
    @DisplayName("피드 생성 (이미지 포함) - 이미지 없이 성공")
    void createFeedWithImages_NoImages_Success() {
        // given
        given(feedCreateService.createFeedWithImages(any(FeedCreateRequestDto.class), any(), eq("testuser")))
                .willReturn(testResponseDto);

        // when
        ApiResponse<FeedCreateResponseDto> response = feedCreateController.createFeedWithImages(
                testRequestDto, null, testUserDetails);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getFeedId()).isEqualTo(1L);

        verify(feedCreateService).createFeedWithImages(any(FeedCreateRequestDto.class), any(), eq("testuser"));
    }
}
