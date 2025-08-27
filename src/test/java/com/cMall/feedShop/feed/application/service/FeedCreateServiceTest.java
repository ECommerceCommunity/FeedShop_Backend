package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
import com.cMall.feedShop.feed.application.exception.OrderItemNotFoundException;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.application.service.FeedMapper;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.feed.application.service.FeedRewardEventHandler;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo;
import com.cMall.feedShop.order.application.service.PurchasedItemService;
import com.cMall.feedShop.order.domain.repository.OrderRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedCreateServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private PurchasedItemService purchasedItemService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FeedMapper feedMapper;

    @Mock
    private FeedImageService feedImageService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private FeedRewardEventHandler feedRewardEventHandler;


    @InjectMocks
    private FeedCreateService feedCreateService;

    private User testUser;
    private FeedCreateRequestDto testRequestDto;
    private Feed testFeed;
    private FeedCreateResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 설정
        testUser = new User();
        testUser.setId(1L);
        testUser.setLoginId("test@example.com");

        // 테스트 요청 DTO 설정
        testRequestDto = FeedCreateRequestDto.builder()
                .title("테스트 피드")
                .content("테스트 피드 내용")
                .orderItemId(1L)
                .eventId(null) // 이벤트 없이 테스트
                .hashtags(Arrays.asList("#테스트", "#피드"))
                .imageUrls(Arrays.asList("image1.jpg", "image2.jpg"))
                .instagramId("test_instagram")
                .build();

        // 테스트 피드 설정
        testFeed = Feed.builder()
                .title("테스트 피드")
                .content("테스트 피드 내용")
                .user(testUser)
                .instagramId("test_instagram")
                .build();

        // 테스트 응답 DTO 설정
        testResponseDto = FeedCreateResponseDto.builder()
                .feedId(1L)
                .title("테스트 피드")
                .content("테스트 피드 내용")
                .build();
    }

    @Test
    @DisplayName("피드 생성 실패 - 사용자를 찾을 수 없음")
    void createFeed_Failure_UserNotFound() {
        // given
        when(userRepository.findByLoginId("nonexistent@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedCreateService.createFeed(testRequestDto, "nonexistent@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("피드 생성 실패 - 구매하지 않은 상품")
    void createFeed_Failure_OrderItemNotFound() {
        // given
        when(userRepository.findByLoginId("test@example.com")).thenReturn(Optional.of(testUser));

        // 빈 목록을 반환하는 응답 객체 생성
        when(purchasedItemService.getPurchasedItems("test@example.com"))
                .thenReturn(mock(com.cMall.feedShop.order.application.dto.response.PurchasedItemListResponse.class));

        // when & then
        assertThatThrownBy(() -> feedCreateService.createFeed(testRequestDto, "test@example.com"))
                .isInstanceOf(OrderItemNotFoundException.class)
                .hasMessageContaining("주문 상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("피드 생성 (기본) - 성공")
    void createFeed_Success() {
        // given
        com.cMall.feedShop.order.application.dto.response.PurchasedItemListResponse mockResponse = 
                mock(com.cMall.feedShop.order.application.dto.response.PurchasedItemListResponse.class);
        List<com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo> mockItems = 
                Arrays.asList(mock(com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo.class));
        
        // OrderItem Mock 설정
        com.cMall.feedShop.order.domain.model.OrderItem mockOrderItem = mock(com.cMall.feedShop.order.domain.model.OrderItem.class);
        when(mockOrderItem.getOrderItemId()).thenReturn(1L);
        
        // Order Mock 설정
        com.cMall.feedShop.order.domain.model.Order mockOrder = mock(com.cMall.feedShop.order.domain.model.Order.class);
        when(mockOrder.getOrderItems()).thenReturn(Arrays.asList(mockOrderItem));
        
        when(userRepository.findByLoginId("test@example.com")).thenReturn(Optional.of(testUser));
        when(purchasedItemService.getPurchasedItems("test@example.com")).thenReturn(mockResponse);
        when(mockResponse.getItems()).thenReturn(mockItems);
        when(mockItems.get(0).getOrderItemId()).thenReturn(1L);
        when(orderRepository.findAll()).thenReturn(Arrays.asList(mockOrder));
        when(feedMapper.toFeed(any(), any(), any(), any())).thenReturn(testFeed);
        when(feedRepository.save(any(Feed.class))).thenReturn(testFeed);
        when(feedMapper.toFeedCreateResponseDto(any(Feed.class))).thenReturn(testResponseDto);

        // when
        FeedCreateResponseDto result = feedCreateService.createFeed(testRequestDto, "test@example.com");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFeedId()).isEqualTo(1L);
        verify(feedImageService, never()).uploadImages(any(Feed.class), any());
    }

    @Test
    @DisplayName("피드 생성 (이미지 포함) - 성공")
    void createFeedWithImages_Success() {
        // given
        List<MultipartFile> testImages = Arrays.asList(
                mock(MultipartFile.class),
                mock(MultipartFile.class)
        );

        com.cMall.feedShop.order.application.dto.response.PurchasedItemListResponse mockResponse = 
                mock(com.cMall.feedShop.order.application.dto.response.PurchasedItemListResponse.class);
        List<com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo> mockItems = 
                Arrays.asList(mock(com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo.class));

        // OrderItem Mock 설정
        com.cMall.feedShop.order.domain.model.OrderItem mockOrderItem = mock(com.cMall.feedShop.order.domain.model.OrderItem.class);
        when(mockOrderItem.getOrderItemId()).thenReturn(1L);
        
        // Order Mock 설정
        com.cMall.feedShop.order.domain.model.Order mockOrder = mock(com.cMall.feedShop.order.domain.model.Order.class);
        when(mockOrder.getOrderItems()).thenReturn(Arrays.asList(mockOrderItem));

        when(userRepository.findByLoginId("test@example.com")).thenReturn(Optional.of(testUser));
        when(purchasedItemService.getPurchasedItems("test@example.com")).thenReturn(mockResponse);
        when(mockResponse.getItems()).thenReturn(mockItems);
        when(mockItems.get(0).getOrderItemId()).thenReturn(1L);
        when(orderRepository.findAll()).thenReturn(Arrays.asList(mockOrder));
        when(feedMapper.toFeed(any(), any(), any(), any())).thenReturn(testFeed);
        when(feedRepository.save(any(Feed.class))).thenReturn(testFeed);
        when(feedMapper.toFeedCreateResponseDto(any(Feed.class))).thenReturn(testResponseDto);

        // when
        FeedCreateResponseDto result = feedCreateService.createFeedWithImages(testRequestDto, testImages, "test@example.com");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFeedId()).isEqualTo(1L);
        verify(feedImageService, times(1)).uploadImages(any(Feed.class), eq(testImages));
    }

    @Test
    @DisplayName("피드 생성 (이미지 없음) - 성공")
    void createFeedWithImages_Success_NoImages() {
        // given
        com.cMall.feedShop.order.application.dto.response.PurchasedItemListResponse mockResponse = 
                mock(com.cMall.feedShop.order.application.dto.response.PurchasedItemListResponse.class);
        List<com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo> mockItems = 
                Arrays.asList(mock(com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo.class));

        // OrderItem Mock 설정
        com.cMall.feedShop.order.domain.model.OrderItem mockOrderItem = mock(com.cMall.feedShop.order.domain.model.OrderItem.class);
        when(mockOrderItem.getOrderItemId()).thenReturn(1L);
        
        // Order Mock 설정
        com.cMall.feedShop.order.domain.model.Order mockOrder = mock(com.cMall.feedShop.order.domain.model.Order.class);
        when(mockOrder.getOrderItems()).thenReturn(Arrays.asList(mockOrderItem));

        when(userRepository.findByLoginId("test@example.com")).thenReturn(Optional.of(testUser));
        when(purchasedItemService.getPurchasedItems("test@example.com")).thenReturn(mockResponse);
        when(mockResponse.getItems()).thenReturn(mockItems);
        when(mockItems.get(0).getOrderItemId()).thenReturn(1L);
        when(orderRepository.findAll()).thenReturn(Arrays.asList(mockOrder));
        when(feedMapper.toFeed(any(), any(), any(), any())).thenReturn(testFeed);
        when(feedRepository.save(any(Feed.class))).thenReturn(testFeed);
        when(feedMapper.toFeedCreateResponseDto(any(Feed.class))).thenReturn(testResponseDto);

        // when
        FeedCreateResponseDto result = feedCreateService.createFeedWithImages(testRequestDto, null, "test@example.com");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFeedId()).isEqualTo(1L);
        verify(feedImageService, never()).uploadImages(any(Feed.class), any());
    }
} 