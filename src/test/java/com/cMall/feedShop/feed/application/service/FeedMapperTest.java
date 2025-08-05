package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
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
} 