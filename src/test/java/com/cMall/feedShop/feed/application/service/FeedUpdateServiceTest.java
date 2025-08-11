package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.request.FeedUpdateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedAccessDeniedException;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedHashtag;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedUpdateServiceTest {

    @Mock private FeedRepository feedRepository;
    @Mock private UserRepository userRepository;
    @Mock private FeedMapper feedMapper;
    @Mock private UserDetails userDetails;

    @InjectMocks private FeedUpdateService feedUpdateService;

    private User owner;
    private User anotherUser;
    private Feed feed;
    private FeedDetailResponseDto responseDto;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "owner_login", "password", "owner@test.com", UserRole.USER);
        anotherUser = new User(2L, "another_login", "password", "another@test.com", UserRole.USER);

        OrderItem orderItem = OrderItem.builder()
                .quantity(1)
                .totalPrice(java.math.BigDecimal.valueOf(10000))
                .finalPrice(java.math.BigDecimal.valueOf(9000))
                .build();

        feed = Feed.builder()
                .user(owner)
                .orderItem(orderItem)
                .title("old title")
                .content("old content")
                .instagramId("old_insta")
                .build();
        // 기존 해시태그 2개
        feed.addHashtags(List.of("old1", "old2"));

        responseDto = FeedDetailResponseDto.builder()
                .feedId(100L)
                .title("new title")
                .content("new content")
                .instagramId("new.insta")
                .build();
    }

    @Test
    @DisplayName("본인 피드 수정 성공 - 본문/해시태그 재구성")
    void updateFeed_Success() {
        // given
        Long feedId = 100L;
        when(userDetails.getUsername()).thenReturn("owner_login");
        when(userRepository.findByLoginId("owner_login")).thenReturn(Optional.of(owner));
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.of(feed));

        FeedUpdateRequestDto dto = FeedUpdateRequestDto.builder()
                .title("new title")
                .content("new content")
                .instagramId("new.insta")
                .hashtags(List.of("#tag1", "tag2", "tag1", " "))
                .build();

        when(feedMapper.toFeedDetailResponseDto(feed)).thenReturn(responseDto);

        // when
        FeedDetailResponseDto result = feedUpdateService.updateFeed(feedId, dto, userDetails);

        // then 본문 값 변경 확인
        assertThat(feed.getTitle()).isEqualTo("new title");
        assertThat(feed.getContent()).isEqualTo("new content");
        assertThat(feed.getInstagramId()).isEqualTo("new.insta");
        // 해시태그: # 제거/공백 필터/중복 제거 → [tag1, tag2]
        List<String> tags = feed.getHashtags().stream().map(FeedHashtag::getTag).collect(Collectors.toList());
        assertThat(tags).containsExactly("tag1", "tag2");

        assertThat(result).isSameAs(responseDto);
    }

    @Test
    @DisplayName("해시태그 null 시 유지")
    void updateFeed_KeepHashtags_WhenNull() {
        Long feedId = 101L;
        when(userDetails.getUsername()).thenReturn("owner_login");
        when(userRepository.findByLoginId("owner_login")).thenReturn(Optional.of(owner));
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.of(feed));

        List<String> before = feed.getHashtags().stream().map(FeedHashtag::getTag).collect(Collectors.toList());

        FeedUpdateRequestDto dto = FeedUpdateRequestDto.builder()
                .title("t")
                .content("c")
                .instagramId("i")
                .hashtags(null)
                .build();
        when(feedMapper.toFeedDetailResponseDto(feed)).thenReturn(responseDto);

        feedUpdateService.updateFeed(feedId, dto, userDetails);

        List<String> after = feed.getHashtags().stream().map(FeedHashtag::getTag).collect(Collectors.toList());
        assertThat(after).isEqualTo(before);
    }

    @Test
    @DisplayName("해시태그 빈 리스트 시 모두 제거")
    void updateFeed_ClearHashtags_WhenEmpty() {
        Long feedId = 102L;
        when(userDetails.getUsername()).thenReturn("owner_login");
        when(userRepository.findByLoginId("owner_login")).thenReturn(Optional.of(owner));
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.of(feed));

        FeedUpdateRequestDto dto = FeedUpdateRequestDto.builder()
                .title("t")
                .content("c")
                .instagramId("i")
                .hashtags(List.of())
                .build();
        when(feedMapper.toFeedDetailResponseDto(feed)).thenReturn(responseDto);

        feedUpdateService.updateFeed(feedId, dto, userDetails);
        assertThat(feed.getHashtags()).isEmpty();
    }

    @Test
    @DisplayName("비소유자 수정 시 403")
    void updateFeed_Forbidden_NotOwner() {
        Long feedId = 103L;
        when(userDetails.getUsername()).thenReturn("another_login");
        when(userRepository.findByLoginId("another_login")).thenReturn(Optional.of(anotherUser));
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.of(feed));

        FeedUpdateRequestDto dto = FeedUpdateRequestDto.builder().title("t").build();
        assertThatThrownBy(() -> feedUpdateService.updateFeed(feedId, dto, userDetails))
                .isInstanceOf(FeedAccessDeniedException.class)
                .hasMessageContaining("본인의 피드만 수정할 수 있습니다");
    }

    @Test
    @DisplayName("피드 미존재 404")
    void updateFeed_NotFound() {
        Long feedId = 104L;
        when(userDetails.getUsername()).thenReturn("owner_login");
        when(userRepository.findByLoginId("owner_login")).thenReturn(Optional.of(owner));
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.empty());

        FeedUpdateRequestDto dto = FeedUpdateRequestDto.builder().title("t").build();
        assertThatThrownBy(() -> feedUpdateService.updateFeed(feedId, dto, userDetails))
                .isInstanceOf(FeedNotFoundException.class);
    }

    @Test
    @DisplayName("이미 삭제된 피드 404")
    void updateFeed_Deleted_NotFound() {
        Long feedId = 105L;
        feed.softDelete();
        when(userDetails.getUsername()).thenReturn("owner_login");
        when(userRepository.findByLoginId("owner_login")).thenReturn(Optional.of(owner));
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.of(feed));

        FeedUpdateRequestDto dto = FeedUpdateRequestDto.builder().title("t").build();
        assertThatThrownBy(() -> feedUpdateService.updateFeed(feedId, dto, userDetails))
                .isInstanceOf(FeedNotFoundException.class);
    }

    @Test
    @DisplayName("미인증 401")
    void updateFeed_Unauthorized() {
        Long feedId = 106L;
        FeedUpdateRequestDto dto = FeedUpdateRequestDto.builder().title("t").build();
        assertThatThrownBy(() -> feedUpdateService.updateFeed(feedId, dto, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    @Test
    @DisplayName("사용자 조회 실패 USER_NOT_FOUND")
    void updateFeed_UserNotFound() {
        Long feedId = 107L;
        when(userDetails.getUsername()).thenReturn("unknown");
        when(userRepository.findByLoginId("unknown")).thenReturn(Optional.empty());

        FeedUpdateRequestDto dto = FeedUpdateRequestDto.builder().title("t").build();
        assertThatThrownBy(() -> feedUpdateService.updateFeed(feedId, dto, userDetails))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }
}
