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
import com.cMall.feedShop.feed.domain.enums.FeedType;
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
                .eventId(1L)
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
} 