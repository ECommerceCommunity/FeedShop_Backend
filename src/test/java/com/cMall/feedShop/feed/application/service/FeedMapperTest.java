package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedType;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class FeedMapperTest {

    private FeedMapper feedMapper;

    private User testUser;
    private OrderItem testOrderItem;
    private Order testOrder;
    private Event testEvent;
    private FeedCreateRequestDto testRequestDto;

    @BeforeEach
    void setUp() {
        feedMapper = new FeedMapper();

        // 테스트 사용자 설정
        testUser = new User();
        testUser.setId(1L);
        testUser.setLoginId("test@example.com");

        // 테스트 주문 아이템 설정 (간단한 테스트용)
        testOrderItem = new OrderItem();

        // 테스트 주문 설정 (간단한 테스트용)
        testOrder = new Order();

        // 테스트 이벤트 설정 (간단한 테스트용)
        testEvent = Event.builder().build();

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
    }

    @Test
    @DisplayName("FeedCreateRequestDto를 Feed 엔티티로 변환 - 이벤트 포함")
    void toFeed_WithEvent() {
        // when
        Feed result = feedMapper.toFeed(testRequestDto, testOrderItem, testUser, testEvent);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("테스트 피드");
        assertThat(result.getContent()).isEqualTo("테스트 피드 내용");
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getOrderItem()).isEqualTo(testOrderItem);
        assertThat(result.getEvent()).isEqualTo(testEvent);
        assertThat(result.getInstagramId()).isEqualTo("test_instagram");
        assertThat(result.getFeedType()).isEqualTo(FeedType.EVENT);
    }

    @Test
    @DisplayName("FeedCreateRequestDto를 Feed 엔티티로 변환 - 이벤트 없음")
    void toFeed_WithoutEvent() {
        // given
        FeedCreateRequestDto requestWithoutEvent = FeedCreateRequestDto.builder()
                .title("테스트 피드")
                .content("테스트 피드 내용")
                .orderItemId(1L)
                .hashtags(Arrays.asList("#테스트", "#피드"))
                .imageUrls(Arrays.asList("image1.jpg", "image2.jpg"))
                .build();

        // when
        Feed result = feedMapper.toFeed(requestWithoutEvent, testOrderItem, testUser, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("테스트 피드");
        assertThat(result.getContent()).isEqualTo("테스트 피드 내용");
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getOrderItem()).isEqualTo(testOrderItem);
        assertThat(result.getEvent()).isNull();
        assertThat(result.getInstagramId()).isNull();
        assertThat(result.getFeedType()).isEqualTo(FeedType.DAILY);
    }

    @Test
    @DisplayName("Feed 엔티티를 FeedCreateResponseDto로 변환")
    void toFeedCreateResponseDto() {
        // given
        Feed feed = Feed.builder()
                .title("테스트 피드")
                .content("테스트 피드 내용")
                .user(testUser)
                .build();

        // when
        FeedCreateResponseDto result = feedMapper.toFeedCreateResponseDto(feed);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("테스트 피드");
        assertThat(result.getContent()).isEqualTo("테스트 피드 내용");
    }



    @Test
    @DisplayName("FeedCreateRequestDto를 Feed 엔티티로 변환 - 해시태그와 이미지 포함")
    void toFeed_WithHashtagsAndImages() {
        // when
        Feed result = feedMapper.toFeed(testRequestDto, testOrderItem, testUser, testEvent);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("테스트 피드");
        assertThat(result.getContent()).isEqualTo("테스트 피드 내용");
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getOrderItem()).isEqualTo(testOrderItem);
        assertThat(result.getEvent()).isEqualTo(testEvent);
        assertThat(result.getInstagramId()).isEqualTo("test_instagram");
        assertThat(result.getFeedType()).isEqualTo(FeedType.EVENT);
    }

    @Test
    @DisplayName("FeedCreateRequestDto를 Feed 엔티티로 변환 - 최소 필드만")
    void toFeed_WithMinimalFields() {
        // given
        FeedCreateRequestDto minimalRequest = FeedCreateRequestDto.builder()
                .title("최소 피드")
                .content("최소 피드 내용")
                .orderItemId(1L)
                .build();

        // when
        Feed result = feedMapper.toFeed(minimalRequest, testOrderItem, testUser, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("최소 피드");
        assertThat(result.getContent()).isEqualTo("최소 피드 내용");
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getOrderItem()).isEqualTo(testOrderItem);
        assertThat(result.getEvent()).isNull();
        assertThat(result.getInstagramId()).isNull();
        assertThat(result.getFeedType()).isEqualTo(FeedType.DAILY);
    }

    @Test
    @DisplayName("toFeedDetailResponseDto - 해시태그/이미지/사용자/상품 정보 매핑 및 정렬/널 세이프티")
    void toFeedDetailResponseDto_basicMapping() {
        // given
        Feed feed = Feed.builder()
                .title("상세 피드")
                .content("상세 내용")
                .instagramId("insta_user")
                .user(testUser)
                .orderItem(OrderItem.builder()
                        .quantity(1)
                        .totalPrice(BigDecimal.valueOf(10000))
                        .finalPrice(BigDecimal.valueOf(9000))
                        .build())
                .build();

        // 해시태그 2개
        feed.addHashtags(Arrays.asList("#태그1", "#태그2"));
        // 이미지 역순 sortOrder로 추가 (정렬 확인용)
        feed.addImage("img2.jpg", 2);
        feed.addImage("img1.jpg", 1);

        // OrderItem-ProductOption-Product 체인 mock (size/productId/imageUrl)
        OrderItem mockedOrderItem = org.mockito.Mockito.mock(OrderItem.class);
        com.cMall.feedShop.product.domain.model.ProductOption mockedOption = org.mockito.Mockito.mock(com.cMall.feedShop.product.domain.model.ProductOption.class);
        com.cMall.feedShop.product.domain.model.Product mockedProduct = org.mockito.Mockito.mock(com.cMall.feedShop.product.domain.model.Product.class);

        org.mockito.Mockito.when(mockedOrderItem.getProductOption()).thenReturn(mockedOption);
        org.mockito.Mockito.when(mockedOption.getSize()).thenReturn(com.cMall.feedShop.product.domain.enums.Size.SIZE_260);
        org.mockito.Mockito.when(mockedOption.getProduct()).thenReturn(mockedProduct);
        org.mockito.Mockito.when(mockedProduct.getMainImageUrl()).thenReturn("http://example.com/main.jpg");
        org.mockito.Mockito.when(mockedProduct.getProductId()).thenReturn(123L);

        // feed의 orderItem을 mock으로 대체
        // (빌더에 넣은 기존 orderItem은 테스트에 영향 없음)
        java.lang.reflect.Field orderItemField;
        try {
            orderItemField = Feed.class.getDeclaredField("orderItem");
            orderItemField.setAccessible(true);
            orderItemField.set(feed, mockedOrderItem);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // when
        FeedDetailResponseDto dto = feedMapper.toFeedDetailResponseDto(feed);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.getTitle()).isEqualTo("상세 피드");
        assertThat(dto.getContent()).isEqualTo("상세 내용");
        // 해시태그
        assertThat(dto.getHashtags()).hasSize(2);
        assertThat(dto.getHashtags().get(0).getTag()).isEqualTo("#태그1");
        assertThat(dto.getHashtags().get(1).getTag()).isEqualTo("#태그2");
        // 이미지 정렬 확인 (sortOrder 1,2 순)
        assertThat(dto.getImages()).hasSize(2);
        assertThat(dto.getImages().get(0).getSortOrder()).isEqualTo(1);
        assertThat(dto.getImages().get(0).getImageUrl()).isEqualTo("img1.jpg");
        assertThat(dto.getImages().get(1).getSortOrder()).isEqualTo(2);
        assertThat(dto.getImages().get(1).getImageUrl()).isEqualTo("img2.jpg");
        // 상품 관련 매핑
        assertThat(dto.getProductSize()).isEqualTo(260);
        assertThat(dto.getProductImageUrl()).isEqualTo("http://example.com/main.jpg");
        assertThat(dto.getProductId()).isEqualTo(123L);
    }

    @Test
    @DisplayName("toFeedDetailResponseDto - orderItem이 null인 경우 널 세이프티")
    void toFeedDetailResponseDto_nullOrderItem() {
        // given
        Feed feed = Feed.builder()
                .title("상세 피드")
                .content("상세 내용")
                .instagramId("insta_user")
                .user(testUser)
                .orderItem(null)
                .build();

        // when
        FeedDetailResponseDto dto = feedMapper.toFeedDetailResponseDto(feed);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.getOrderItemId()).isNull();
        assertThat(dto.getProductName()).isEqualTo("알 수 없는 상품");
        assertThat(dto.getProductSize()).isNull();
        assertThat(dto.getProductImageUrl()).isNull();
        assertThat(dto.getProductId()).isNull();
    }
} 