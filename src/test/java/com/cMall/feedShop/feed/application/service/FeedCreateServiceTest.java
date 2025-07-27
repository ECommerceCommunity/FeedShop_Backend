package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
import com.cMall.feedShop.feed.application.exception.DuplicateFeedException;
import com.cMall.feedShop.feed.application.exception.EventNotAvailableException;
import com.cMall.feedShop.feed.application.exception.OrderItemNotFoundException;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.order.application.dto.response.PurchasedItemListResponse;
import com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo;
import com.cMall.feedShop.order.application.service.PurchasedItemService;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.order.domain.repository.OrderItemRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedCreateService 단위 테스트")
class FeedCreateServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private PurchasedItemService purchasedItemService;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private FeedMapper feedMapper;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private FeedCreateService feedCreateService;

    private User user;
    private OrderItem orderItem;
    private Event event;
    private FeedCreateRequestDto requestDto;
    private FeedCreateResponseDto responseDto;
    private Feed feed;
    private PurchasedItemInfo purchasedItemInfo;

    @BeforeEach
    void setUp() {
        // 사용자 설정
        user = new User("testuser", "password", "test@test.com", com.cMall.feedShop.user.domain.enums.UserRole.USER);
        user.setId(1L);
        UserProfile userProfile = new UserProfile(user, "테스트", "테스트닉네임", "010-1234-5678");
        user.setUserProfile(userProfile);

        // 주문 아이템 설정
        Order order = Order.builder()
                .user(user)
                .status(com.cMall.feedShop.order.domain.enums.OrderStatus.ORDERED)
                .finalPrice(BigDecimal.valueOf(9000))
                .totalPrice(BigDecimal.valueOf(10000))
                .deliveryAddress("테스트 주소")
                .deliveryDetailAddress("상세 주소")
                .postalCode("12345")
                .recipientName("수령인")
                .recipientPhone("010-1234-5678")
                .paymentMethod("CARD")
                .build();
        orderItem = OrderItem.builder()
                .order(order)
                .productOption(null)
                .productImage(null)
                .quantity(1)
                .totalPrice(BigDecimal.valueOf(10000))
                .finalPrice(BigDecimal.valueOf(9000))
                .build();

        // 이벤트 설정
        EventDetail eventDetail = EventDetail.builder()
                .title("테스트 이벤트")
                .eventStartDate(LocalDate.now())
                .eventEndDate(LocalDate.now().plusDays(7))
                .build();
        event = Event.builder()
                .type(EventType.BATTLE)
                .status(EventStatus.ONGOING)
                .eventDetail(eventDetail)
                .build();

        // 요청 DTO 설정
        requestDto = FeedCreateRequestDto.builder()
                .title("테스트 피드")
                .content("테스트 내용")
                .orderItemId(1L)
                .eventId(1L)
                .hashtags(Arrays.asList("테스트", "피드"))
                .imageUrls(Arrays.asList("http://image1.jpg", "http://image2.jpg"))
                .build();

        // 응답 DTO 설정
        responseDto = FeedCreateResponseDto.builder()
                .feedId(1L)
                .title("테스트 피드")
                .content("테스트 내용")
                .feedType(FeedType.EVENT)
                .userId(1L)
                .userNickname("테스트닉네임")
                .orderItemId(1L)
                .productName("테스트 상품")
                .eventId(1L)
                .eventTitle("테스트 이벤트")
                .hashtags(Arrays.asList("테스트", "피드"))
                .imageUrls(Arrays.asList("http://image1.jpg", "http://image2.jpg"))
                .build();

        // 피드 설정
        feed = Feed.builder()
                .event(event)
                .orderItem(orderItem)
                .user(user)
                .title("테스트 피드")
                .content("테스트 내용")
                .build();

        // 구매 상품 정보 설정
        purchasedItemInfo = PurchasedItemInfo.builder()
                .orderItemId(1L)
                .productId(1L)
                .productName("테스트 상품")
                .productImageUrl("http://product.jpg")
                .orderedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("피드 생성 성공 - 이벤트 참여")
    void createFeed_Success_WithEvent() {
        // Given
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(purchasedItemService.getPurchasedItems(userDetails))
                .thenReturn(PurchasedItemListResponse.from(Arrays.asList(purchasedItemInfo)));
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));
        when(feedRepository.existsByOrderItemIdAndUserId(1L, 1L)).thenReturn(false);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(feedMapper.toFeed(requestDto, orderItem, user, event)).thenReturn(feed);
        when(feedRepository.save(feed)).thenReturn(feed);
        when(feedMapper.toFeedCreateResponseDto(feed)).thenReturn(responseDto);

        // When
        FeedCreateResponseDto result = feedCreateService.createFeed(requestDto, userDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFeedId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("테스트 피드");
        assertThat(result.getFeedType()).isEqualTo(FeedType.EVENT);
        assertThat(result.getEventId()).isEqualTo(1L);
        assertThat(result.getHashtags()).hasSize(2);
        assertThat(result.getImageUrls()).hasSize(2);
    }

    @Test
    @DisplayName("피드 생성 성공 - 일상 피드")
    void createFeed_Success_DailyFeed() {
        // Given
        requestDto = FeedCreateRequestDto.builder()
                .title("테스트 피드")
                .content("테스트 내용")
                .orderItemId(1L)
                .hashtags(Arrays.asList("테스트", "피드"))
                .imageUrls(Arrays.asList("http://image1.jpg", "http://image2.jpg"))
                .build();

        responseDto = FeedCreateResponseDto.builder()
                .feedId(1L)
                .title("테스트 피드")
                .content("테스트 내용")
                .feedType(FeedType.DAILY)
                .userId(1L)
                .userNickname("테스트닉네임")
                .orderItemId(1L)
                .productName("테스트 상품")
                .hashtags(Arrays.asList("테스트", "피드"))
                .imageUrls(Arrays.asList("http://image1.jpg", "http://image2.jpg"))
                .build();

        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(purchasedItemService.getPurchasedItems(userDetails))
                .thenReturn(PurchasedItemListResponse.from(Arrays.asList(purchasedItemInfo)));
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));
        when(feedRepository.existsByOrderItemIdAndUserId(1L, 1L)).thenReturn(false);
        when(feedMapper.toFeed(requestDto, orderItem, user, null)).thenReturn(feed);
        when(feedRepository.save(feed)).thenReturn(feed);
        when(feedMapper.toFeedCreateResponseDto(feed)).thenReturn(responseDto);

        // When
        FeedCreateResponseDto result = feedCreateService.createFeed(requestDto, userDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFeedId()).isEqualTo(1L);
        assertThat(result.getFeedType()).isEqualTo(FeedType.DAILY);
        assertThat(result.getEventId()).isNull();
    }

    @Test
    @DisplayName("피드 생성 실패 - 사용자 없음")
    void createFeed_UserNotFound() {
        // Given
        when(userDetails.getUsername()).thenReturn("nonexistent");
        when(userRepository.findByLoginId("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> feedCreateService.createFeed(requestDto, userDetails))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("피드 생성 실패 - 주문 상품 없음")
    void createFeed_OrderItemNotFound() {
        // Given
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(purchasedItemService.getPurchasedItems(userDetails))
                .thenReturn(PurchasedItemListResponse.from(Arrays.asList()));

        // When & Then
        assertThatThrownBy(() -> feedCreateService.createFeed(requestDto, userDetails))
                .isInstanceOf(OrderItemNotFoundException.class);
    }

    @Test
    @DisplayName("피드 생성 실패 - 중복 피드")
    void createFeed_DuplicateFeed() {
        // Given
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(purchasedItemService.getPurchasedItems(userDetails))
                .thenReturn(PurchasedItemListResponse.from(Arrays.asList(purchasedItemInfo)));
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));
        when(feedRepository.existsByOrderItemIdAndUserId(1L, 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> feedCreateService.createFeed(requestDto, userDetails))
                .isInstanceOf(DuplicateFeedException.class);
    }

    @Test
    @DisplayName("피드 생성 실패 - 이벤트 없음")
    void createFeed_EventNotFound() {
        // Given
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(purchasedItemService.getPurchasedItems(userDetails))
                .thenReturn(PurchasedItemListResponse.from(Arrays.asList(purchasedItemInfo)));
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));
        when(feedRepository.existsByOrderItemIdAndUserId(1L, 1L)).thenReturn(false);
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> feedCreateService.createFeed(requestDto, userDetails))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EVENT_NOT_FOUND);
    }

    @Test
    @DisplayName("피드 생성 실패 - 이벤트 참여 불가 (종료된 이벤트)")
    void createFeed_EventNotAvailable_EndedEvent() {
        // Given
        EventDetail endedEventDetail = EventDetail.builder()
                .title("종료된 이벤트")
                .eventStartDate(LocalDate.now().minusDays(10))
                .eventEndDate(LocalDate.now().minusDays(1))
                .build();
        Event endedEvent = Event.builder()
                .type(EventType.BATTLE)
                .status(EventStatus.ONGOING)
                .eventDetail(endedEventDetail)
                .build();

        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(purchasedItemService.getPurchasedItems(userDetails))
                .thenReturn(PurchasedItemListResponse.from(Arrays.asList(purchasedItemInfo)));
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));
        when(feedRepository.existsByOrderItemIdAndUserId(1L, 1L)).thenReturn(false);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(endedEvent));

        // When & Then
        assertThatThrownBy(() -> feedCreateService.createFeed(requestDto, userDetails))
                .isInstanceOf(EventNotAvailableException.class);
    }

    @Test
    @DisplayName("피드 생성 성공 - UserProfile이 null인 경우")
    void createFeed_Success_WithNullUserProfile() {
        // Given - UserProfile이 null인 사용자 설정
        User userWithoutProfile = new User("testuser", "password", "test@test.com", com.cMall.feedShop.user.domain.enums.UserRole.USER);
        userWithoutProfile.setId(1L);
        // UserProfile 설정하지 않음 (null 상태)

        FeedCreateResponseDto responseDtoWithNullProfile = FeedCreateResponseDto.builder()
                .feedId(1L)
                .title("테스트 피드")
                .content("테스트 내용")
                .feedType(FeedType.EVENT)
                .userId(1L)
                .userNickname("알 수 없음") // null일 때 기본값
                .orderItemId(1L)
                .productName("알 수 없는 상품") // null일 때 기본값
                .eventId(1L)
                .eventTitle("테스트 이벤트")
                .hashtags(Arrays.asList("테스트", "피드"))
                .imageUrls(Arrays.asList("http://image1.jpg", "http://image2.jpg"))
                .build();

        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(userWithoutProfile));
        when(purchasedItemService.getPurchasedItems(userDetails))
                .thenReturn(PurchasedItemListResponse.from(Arrays.asList(purchasedItemInfo)));
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));
        when(feedRepository.existsByOrderItemIdAndUserId(1L, 1L)).thenReturn(false);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(feedMapper.toFeed(requestDto, orderItem, userWithoutProfile, event)).thenReturn(feed);
        when(feedRepository.save(feed)).thenReturn(feed);
        when(feedMapper.toFeedCreateResponseDto(feed)).thenReturn(responseDtoWithNullProfile);

        // When
        FeedCreateResponseDto result = feedCreateService.createFeed(requestDto, userDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFeedId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("테스트 피드");
        assertThat(result.getFeedType()).isEqualTo(FeedType.EVENT);
        assertThat(result.getUserNickname()).isEqualTo("알 수 없음");
        assertThat(result.getProductName()).isEqualTo("알 수 없는 상품");
    }
} 