package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedHashtag;
import com.cMall.feedShop.feed.domain.FeedImage;
import com.cMall.feedShop.feed.domain.FeedType;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedMapper 단위 테스트")
class FeedMapperTest {

    @InjectMocks
    private FeedMapper feedMapper;

    private FeedCreateRequestDto requestDto;
    private User userWithProfile;
    private User userWithoutProfile;
    private UserProfile userProfile;
    private OrderItem orderItem;
    private ProductOption productOption;
    private Product product;
    private Event event;
    private EventDetail eventDetail;

    @BeforeEach
    void setUp() {
        // UserProfile 설정
        userProfile = new UserProfile(null, "테스트이름", "테스트닉네임", "010-1234-5678");

        // User 설정 (UserProfile 있음)
        userWithProfile = new User("testuser", "password", "test@test.com", com.cMall.feedShop.user.domain.enums.UserRole.USER);
        userWithProfile.setId(1L);
        userWithProfile.setUserProfile(userProfile);

        // User 설정 (UserProfile 없음)
        userWithoutProfile = new User("testuser2", "password", "test2@test.com", com.cMall.feedShop.user.domain.enums.UserRole.USER);
        userWithoutProfile.setId(2L);
        userWithoutProfile.setUserProfile(null);

        // Product 설정
        product = Product.builder()
                .name("테스트 상품")
                .build();

        // ProductOption 설정
        productOption = new ProductOption(null, null, null, 10, product);

        // OrderItem 설정
        orderItem = OrderItem.builder()
                .productOption(productOption)
                .build();
        ReflectionTestUtils.setField(orderItem, "orderItemId", 1L);

        // EventDetail 설정
        eventDetail = EventDetail.builder()
                .title("테스트 이벤트")
                .build();

        // Event 설정
        event = Event.builder()
                .eventDetail(eventDetail)
                .build();
        ReflectionTestUtils.setField(event, "id", 1L);

        // RequestDto 설정
        requestDto = FeedCreateRequestDto.builder()
                .title("테스트 피드")
                .content("테스트 내용")
                .instagramId("test_instagram")
                .build();
    }

    @Test
    @DisplayName("Feed 엔티티 생성 성공")
    void toFeed_Success() {
        // When
        Feed feed = feedMapper.toFeed(requestDto, orderItem, userWithProfile, event);

        // Then
        assertThat(feed).isNotNull();
        assertThat(feed.getEvent()).isEqualTo(event);
        assertThat(feed.getOrderItem()).isEqualTo(orderItem);
        assertThat(feed.getUser()).isEqualTo(userWithProfile);
        assertThat(feed.getTitle()).isEqualTo("테스트 피드");
        assertThat(feed.getContent()).isEqualTo("테스트 내용");
        assertThat(feed.getInstagramId()).isEqualTo("test_instagram");
        assertThat(feed.getFeedType()).isEqualTo(FeedType.EVENT); // 이벤트가 있으므로 EVENT 타입
    }

    @Test
    @DisplayName("Feed 엔티티 생성 성공 - 이벤트 없음")
    void toFeed_Success_WithoutEvent() {
        // When
        Feed feed = feedMapper.toFeed(requestDto, orderItem, userWithProfile, null);

        // Then
        assertThat(feed).isNotNull();
        assertThat(feed.getEvent()).isNull();
        assertThat(feed.getOrderItem()).isEqualTo(orderItem);
        assertThat(feed.getUser()).isEqualTo(userWithProfile);
        assertThat(feed.getTitle()).isEqualTo("테스트 피드");
        assertThat(feed.getContent()).isEqualTo("테스트 내용");
        assertThat(feed.getInstagramId()).isEqualTo("test_instagram");
        assertThat(feed.getFeedType()).isEqualTo(FeedType.DAILY); // 이벤트가 없으므로 DAILY 타입
    }

    @Test
    @DisplayName("FeedCreateResponseDto 변환 성공 - 모든 데이터 있음")
    void toFeedCreateResponseDto_Success_WithAllData() {
        // Given
        Feed feed = Feed.builder()
                .event(event)
                .orderItem(orderItem)
                .user(userWithProfile)
                .title("테스트 피드")
                .content("테스트 내용")
                .instagramId("test_instagram")
                .build();
        
        ReflectionTestUtils.setField(feed, "id", 1L);
        ReflectionTestUtils.setField(feed, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0, 0));
        ReflectionTestUtils.setField(feed, "feedType", FeedType.EVENT);
        ReflectionTestUtils.setField(feed, "hashtags", new ArrayList<>());
        ReflectionTestUtils.setField(feed, "images", new ArrayList<>());

        // When
        FeedCreateResponseDto responseDto = feedMapper.toFeedCreateResponseDto(feed);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getFeedId()).isEqualTo(1L);
        assertThat(responseDto.getTitle()).isEqualTo("테스트 피드");
        assertThat(responseDto.getContent()).isEqualTo("테스트 내용");
        assertThat(responseDto.getFeedType()).isEqualTo(FeedType.EVENT);
        assertThat(responseDto.getInstagramId()).isEqualTo("test_instagram");
        assertThat(responseDto.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0, 0));
        assertThat(responseDto.getUserId()).isEqualTo(1L);
        assertThat(responseDto.getUserNickname()).isEqualTo("테스트닉네임");
        assertThat(responseDto.getOrderItemId()).isEqualTo(1L);
        assertThat(responseDto.getProductName()).isEqualTo("테스트 상품");
        assertThat(responseDto.getEventId()).isEqualTo(1L);
        assertThat(responseDto.getEventTitle()).isEqualTo("테스트 이벤트");
        assertThat(responseDto.getHashtags()).isEmpty();
        assertThat(responseDto.getImageUrls()).isEmpty();
    }

    @Test
    @DisplayName("FeedCreateResponseDto 변환 성공 - UserProfile이 null인 경우")
    void toFeedCreateResponseDto_Success_WithNullUserProfile() {
        // Given
        Feed feed = Feed.builder()
                .event(event)
                .orderItem(orderItem)
                .user(userWithoutProfile) // UserProfile이 null인 사용자
                .title("테스트 피드")
                .content("테스트 내용")
                .build();
        
        ReflectionTestUtils.setField(feed, "id", 2L);
        ReflectionTestUtils.setField(feed, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0, 0));
        ReflectionTestUtils.setField(feed, "feedType", FeedType.EVENT);
        ReflectionTestUtils.setField(feed, "hashtags", new ArrayList<>());
        ReflectionTestUtils.setField(feed, "images", new ArrayList<>());

        // When
        FeedCreateResponseDto responseDto = feedMapper.toFeedCreateResponseDto(feed);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getFeedId()).isEqualTo(2L);
        assertThat(responseDto.getTitle()).isEqualTo("테스트 피드");
        assertThat(responseDto.getContent()).isEqualTo("테스트 내용");
        assertThat(responseDto.getUserId()).isEqualTo(2L);
        assertThat(responseDto.getUserNickname()).isEqualTo("알 수 없음"); // null일 때 기본값
        assertThat(responseDto.getEventId()).isEqualTo(1L);
        assertThat(responseDto.getEventTitle()).isEqualTo("테스트 이벤트");
    }

    @Test
    @DisplayName("FeedCreateResponseDto 변환 성공 - Event가 null인 경우")
    void toFeedCreateResponseDto_Success_WithNullEvent() {
        // Given
        Feed feed = Feed.builder()
                .event(null) // Event가 null
                .orderItem(orderItem)
                .user(userWithProfile)
                .title("테스트 피드")
                .content("테스트 내용")
                .build();
        
        ReflectionTestUtils.setField(feed, "id", 3L);
        ReflectionTestUtils.setField(feed, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0, 0));
        ReflectionTestUtils.setField(feed, "feedType", FeedType.DAILY);
        ReflectionTestUtils.setField(feed, "hashtags", new ArrayList<>());
        ReflectionTestUtils.setField(feed, "images", new ArrayList<>());

        // When
        FeedCreateResponseDto responseDto = feedMapper.toFeedCreateResponseDto(feed);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getFeedId()).isEqualTo(3L);
        assertThat(responseDto.getTitle()).isEqualTo("테스트 피드");
        assertThat(responseDto.getContent()).isEqualTo("테스트 내용");
        assertThat(responseDto.getUserId()).isEqualTo(1L);
        assertThat(responseDto.getUserNickname()).isEqualTo("테스트닉네임");
        assertThat(responseDto.getEventId()).isNull(); // Event가 null이므로 null
        assertThat(responseDto.getEventTitle()).isNull(); // Event가 null이므로 null
    }

    @Test
    @DisplayName("FeedCreateResponseDto 변환 성공 - OrderItem이 null인 경우")
    void toFeedCreateResponseDto_Success_WithNullOrderItem() {
        // Given
        Feed feed = Feed.builder()
                .event(event)
                .orderItem(null) // OrderItem이 null
                .user(userWithProfile)
                .title("테스트 피드")
                .content("테스트 내용")
                .build();
        
        ReflectionTestUtils.setField(feed, "id", 4L);
        ReflectionTestUtils.setField(feed, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0, 0));
        ReflectionTestUtils.setField(feed, "feedType", FeedType.EVENT);
        ReflectionTestUtils.setField(feed, "hashtags", new ArrayList<>());
        ReflectionTestUtils.setField(feed, "images", new ArrayList<>());

        // When
        FeedCreateResponseDto responseDto = feedMapper.toFeedCreateResponseDto(feed);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getFeedId()).isEqualTo(4L);
        assertThat(responseDto.getTitle()).isEqualTo("테스트 피드");
        assertThat(responseDto.getContent()).isEqualTo("테스트 내용");
        assertThat(responseDto.getUserId()).isEqualTo(1L);
        assertThat(responseDto.getUserNickname()).isEqualTo("테스트닉네임");
        assertThat(responseDto.getOrderItemId()).isNull(); // OrderItem이 null이므로 null
        assertThat(responseDto.getProductName()).isEqualTo("알 수 없는 상품"); // OrderItem이 null이므로 기본값
        assertThat(responseDto.getEventId()).isEqualTo(1L);
        assertThat(responseDto.getEventTitle()).isEqualTo("테스트 이벤트");
    }

    @Test
    @DisplayName("FeedCreateResponseDto 변환 성공 - 모든 연관 엔티티가 null인 경우")
    void toFeedCreateResponseDto_Success_WithAllNullAssociations() {
        // Given
        Feed feed = Feed.builder()
                .event(null)
                .orderItem(null)
                .user(userWithoutProfile)
                .title("테스트 피드")
                .content("테스트 내용")
                .build();
        
        ReflectionTestUtils.setField(feed, "id", 5L);
        ReflectionTestUtils.setField(feed, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0, 0));
        ReflectionTestUtils.setField(feed, "feedType", FeedType.DAILY);
        ReflectionTestUtils.setField(feed, "hashtags", new ArrayList<>());
        ReflectionTestUtils.setField(feed, "images", new ArrayList<>());

        // When
        FeedCreateResponseDto responseDto = feedMapper.toFeedCreateResponseDto(feed);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getFeedId()).isEqualTo(5L);
        assertThat(responseDto.getTitle()).isEqualTo("테스트 피드");
        assertThat(responseDto.getContent()).isEqualTo("테스트 내용");
        assertThat(responseDto.getUserId()).isEqualTo(2L);
        assertThat(responseDto.getUserNickname()).isEqualTo("알 수 없음");
        assertThat(responseDto.getOrderItemId()).isNull();
        assertThat(responseDto.getProductName()).isEqualTo("알 수 없는 상품");
        assertThat(responseDto.getEventId()).isNull();
        assertThat(responseDto.getEventTitle()).isNull();
    }

    @Test
    @DisplayName("FeedCreateResponseDto 변환 성공 - null 필드들이 있는 경우")
    void toFeedCreateResponseDto_Success_WithNullFields() {
        // Given
        Feed feed = Feed.builder()
                .event(event)
                .orderItem(orderItem)
                .user(userWithProfile)
                .title(null)
                .content(null)
                .instagramId(null)
                .build();
        
        ReflectionTestUtils.setField(feed, "id", null);
        ReflectionTestUtils.setField(feed, "createdAt", null);
        ReflectionTestUtils.setField(feed, "feedType", null);
        ReflectionTestUtils.setField(feed, "hashtags", null);
        ReflectionTestUtils.setField(feed, "images", null);

        // When
        FeedCreateResponseDto responseDto = feedMapper.toFeedCreateResponseDto(feed);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getFeedId()).isEqualTo(0L); // null이므로 기본값
        assertThat(responseDto.getTitle()).isEqualTo(""); // null이므로 기본값
        assertThat(responseDto.getContent()).isEqualTo(""); // null이므로 기본값
        assertThat(responseDto.getFeedType()).isEqualTo(FeedType.DAILY); // null이므로 기본값
        assertThat(responseDto.getInstagramId()).isNull();
        assertThat(responseDto.getCreatedAt()).isNotNull(); // null이므로 현재 시간
        assertThat(responseDto.getUserId()).isEqualTo(1L);
        assertThat(responseDto.getUserNickname()).isEqualTo("테스트닉네임");
        assertThat(responseDto.getOrderItemId()).isEqualTo(1L);
        assertThat(responseDto.getProductName()).isEqualTo("테스트 상품");
        assertThat(responseDto.getEventId()).isEqualTo(1L);
        assertThat(responseDto.getEventTitle()).isEqualTo("테스트 이벤트");
        assertThat(responseDto.getHashtags()).isEmpty(); // null이므로 빈 리스트
        assertThat(responseDto.getImageUrls()).isEmpty(); // null이므로 빈 리스트
    }

    @Test
    @DisplayName("FeedCreateResponseDto 변환 성공 - 해시태그와 이미지가 있는 경우")
    void toFeedCreateResponseDto_Success_WithHashtagsAndImages() {
        // Given
        List<FeedHashtag> hashtags = new ArrayList<>();
        hashtags.add(FeedHashtag.builder().tag("태그1").build());
        hashtags.add(FeedHashtag.builder().tag("태그2").build());

        List<FeedImage> images = new ArrayList<>();
        images.add(FeedImage.builder().imageUrl("image1.jpg").sortOrder(1).build());
        images.add(FeedImage.builder().imageUrl("image2.jpg").sortOrder(2).build());

        Feed feed = Feed.builder()
                .event(event)
                .orderItem(orderItem)
                .user(userWithProfile)
                .title("테스트 피드")
                .content("테스트 내용")
                .build();
        
        ReflectionTestUtils.setField(feed, "id", 6L);
        ReflectionTestUtils.setField(feed, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0, 0));
        ReflectionTestUtils.setField(feed, "feedType", FeedType.EVENT);
        ReflectionTestUtils.setField(feed, "hashtags", hashtags);
        ReflectionTestUtils.setField(feed, "images", images);

        // When
        FeedCreateResponseDto responseDto = feedMapper.toFeedCreateResponseDto(feed);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getFeedId()).isEqualTo(6L);
        assertThat(responseDto.getTitle()).isEqualTo("테스트 피드");
        assertThat(responseDto.getContent()).isEqualTo("테스트 내용");
        assertThat(responseDto.getUserId()).isEqualTo(1L);
        assertThat(responseDto.getUserNickname()).isEqualTo("테스트닉네임");
        assertThat(responseDto.getOrderItemId()).isEqualTo(1L);
        assertThat(responseDto.getProductName()).isEqualTo("테스트 상품");
        assertThat(responseDto.getEventId()).isEqualTo(1L);
        assertThat(responseDto.getEventTitle()).isEqualTo("테스트 이벤트");
        assertThat(responseDto.getHashtags()).containsExactly("태그1", "태그2");
        assertThat(responseDto.getImageUrls()).containsExactly("image1.jpg", "image2.jpg");
    }
} 