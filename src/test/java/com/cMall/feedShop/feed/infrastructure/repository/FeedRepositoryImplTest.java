package com.cMall.feedShop.feed.infrastructure.repository;

import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.entity.FeedImage;
import com.cMall.feedShop.feed.domain.entity.FeedHashtag;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedJpaRepository;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.infrastructure.repository.FeedQueryRepositoryImpl;
import com.cMall.feedShop.feed.domain.repository.FeedRepositoryImpl;
import com.cMall.feedShop.feed.application.dto.request.FeedSearchRequest;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedRepositoryImpl 테스트")
class FeedRepositoryImplTest {

    @Mock
    private FeedJpaRepository feedJpaRepository;

    @Mock
    private FeedQueryRepositoryImpl feedQueryRepository;

    @InjectMocks
    private FeedRepositoryImpl feedRepository;

    private Feed testFeed;
    private User testUser;
    private OrderItem testOrderItem;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .loginId("testuser")
                .password("password")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // 테스트용 주문 아이템 생성
        testOrderItem = OrderItem.builder()
                .order(mock(Order.class))
                .productOption(mock(ProductOption.class))
                .productImage(mock(ProductImage.class))
                .quantity(1)
                .totalPrice(BigDecimal.valueOf(10000))
                .finalPrice(BigDecimal.valueOf(10000))
                .build();
        ReflectionTestUtils.setField(testOrderItem, "orderItemId", 1L);

        // 테스트용 이벤트 생성
        testEvent = Event.builder()
                .id(1L)
                .build();

        // 테스트용 피드 생성
        testFeed = Feed.builder()
                .event(testEvent)
                .orderItem(testOrderItem)
                .user(testUser)
                .title("테스트 피드")
                .content("테스트 피드 내용입니다.")
                .instagramId("test_instagram")
                .build();
        ReflectionTestUtils.setField(testFeed, "id", 1L);
    }

    @Test
    @DisplayName("피드를 성공적으로 저장할 수 있다")
    void saveFeed() {
        // given
        given(feedJpaRepository.save(any(Feed.class))).willReturn(testFeed);

        // when
        Feed savedFeed = feedRepository.save(testFeed);

        // then
        assertThat(savedFeed).isEqualTo(testFeed);
        verify(feedJpaRepository, times(1)).save(testFeed);
    }

    @Test
    @DisplayName("ID로 피드를 조회할 수 있다")
    void findById() {
        // given
        given(feedJpaRepository.findById(1L)).willReturn(Optional.of(testFeed));

        // when
        Optional<Feed> foundFeed = feedRepository.findById(1L);

        // then
        assertThat(foundFeed).isPresent();
        assertThat(foundFeed.get()).isEqualTo(testFeed);
        verify(feedJpaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("피드를 삭제할 수 있다")
    void deleteFeed() {
        // when
        feedRepository.delete(testFeed);

        // then
        verify(feedJpaRepository, times(1)).delete(testFeed);
    }

    @Test
    @DisplayName("주문 아이템과 사용자로 피드 존재 여부를 확인할 수 있다")
    void existsByOrderItemIdAndUserId() {
        // given
        given(feedJpaRepository.existsByOrderItemOrderItemIdAndUserId(1L, 1L)).willReturn(true);

        // when
        boolean exists = feedRepository.existsByOrderItemIdAndUserId(1L, 1L);

        // then
        assertThat(exists).isTrue();
        verify(feedJpaRepository, times(1)).existsByOrderItemOrderItemIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("페이징으로 모든 피드를 조회할 수 있다")
    void findAll() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Feed> feedPage = new PageImpl<>(Arrays.asList(testFeed), pageable, 1);
        given(feedJpaRepository.findAllActive(pageable)).willReturn(feedPage);

        // when
        Page<Feed> result = feedRepository.findAll(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testFeed);
        verify(feedJpaRepository, times(1)).findAllActive(pageable);
    }

    @Test
    @DisplayName("사용자별 피드를 페이징으로 조회할 수 있다")
    void findByUserId() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Feed> feedPage = new PageImpl<>(Arrays.asList(testFeed), pageable, 1);
        given(feedJpaRepository.findByUserId(1L, pageable)).willReturn(feedPage);

        // when
        Page<Feed> result = feedRepository.findByUserId(1L, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testFeed);
        verify(feedJpaRepository, times(1)).findByUserId(1L, pageable);
    }

    @Test
    @DisplayName("피드 타입별로 피드를 페이징으로 조회할 수 있다")
    void findByFeedType() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Feed> feedPage = new PageImpl<>(Arrays.asList(testFeed), pageable, 1);
        given(feedJpaRepository.findByFeedType(FeedType.EVENT, pageable)).willReturn(feedPage);

        // when
        Page<Feed> result = feedRepository.findByFeedType("EVENT", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testFeed);
        verify(feedJpaRepository, times(1)).findByFeedType(FeedType.EVENT, pageable);
    }

    @Test
    @DisplayName("이벤트별 피드 목록을 조회할 수 있다")
    void findByEventId() {
        // given
        List<Feed> feeds = Arrays.asList(testFeed);
        given(feedJpaRepository.findByEventId(1L)).willReturn(feeds);

        // when
        List<Feed> result = feedRepository.findByEventId(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testFeed);
        verify(feedJpaRepository, times(1)).findByEventId(1L);
    }

    @Test
    @DisplayName("주문 아이템별 피드 목록을 조회할 수 있다")
    void findByOrderItemId() {
        // given
        List<Feed> feeds = Arrays.asList(testFeed);
        given(feedJpaRepository.findByOrderItemOrderItemId(1L)).willReturn(feeds);

        // when
        List<Feed> result = feedRepository.findByOrderItemId(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testFeed);
        verify(feedJpaRepository, times(1)).findByOrderItemOrderItemId(1L);
    }

    @Test
    @DisplayName("피드 상세 정보를 조회할 수 있다")
    void findDetailById() {
        // given
        given(feedJpaRepository.findDetailById(1L)).willReturn(Optional.of(testFeed));

        // when
        Optional<Feed> result = feedRepository.findDetailById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testFeed);
        verify(feedJpaRepository, times(1)).findDetailById(1L);
    }

    @Test
    @DisplayName("모든 관계 엔티티를 포함한 피드 상세 정보를 조회할 수 있다")
    void findDetailWithAllById() {
        // given
        given(feedJpaRepository.findDetailWithAllById(1L)).willReturn(Optional.of(testFeed));

        // when
        Optional<Feed> result = feedRepository.findDetailWithAllById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testFeed);
        verify(feedJpaRepository, times(1)).findDetailWithAllById(1L);
    }

    @Test
    @DisplayName("사용자와 피드 타입별로 피드를 페이징으로 조회할 수 있다")
    void findByUserIdAndFeedType() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Feed> feedPage = new PageImpl<>(Arrays.asList(testFeed), pageable, 1);
        given(feedJpaRepository.findByUserIdAndFeedTypeActive(1L, FeedType.EVENT, pageable)).willReturn(feedPage);

        // when
        Page<Feed> result = feedRepository.findByUserIdAndFeedType(1L, "EVENT", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testFeed);
        verify(feedJpaRepository, times(1)).findByUserIdAndFeedTypeActive(1L, FeedType.EVENT, pageable);
    }

    @Test
    @DisplayName("사용자별 피드 개수를 조회할 수 있다")
    void countByUserId() {
        // given
        given(feedJpaRepository.countByUserIdActive(1L)).willReturn(5L);

        // when
        long count = feedRepository.countByUserId(1L);

        // then
        assertThat(count).isEqualTo(5L);
        verify(feedJpaRepository, times(1)).countByUserIdActive(1L);
    }

    @Test
    @DisplayName("사용자와 피드 타입별 피드 개수를 조회할 수 있다")
    void countByUserIdAndFeedType() {
        // given
        given(feedJpaRepository.countByUserIdAndFeedTypeActive(1L, FeedType.EVENT)).willReturn(3L);

        // when
        long count = feedRepository.countByUserIdAndFeedType(1L, "EVENT");

        // then
        assertThat(count).isEqualTo(3L);
        verify(feedJpaRepository, times(1)).countByUserIdAndFeedTypeActive(1L, FeedType.EVENT);
    }

    @Test
    @DisplayName("검색 조건에 따른 피드 개수를 조회할 수 있다")
    void countWithSearchConditions() {
        // given
        given(feedQueryRepository.countWithSearchConditions(any())).willReturn(10L);

        // when
        long count = feedRepository.countWithSearchConditions(any());

        // then
        assertThat(count).isEqualTo(10L);
        verify(feedQueryRepository, times(1)).countWithSearchConditions(any());
    }

    @Test
    @DisplayName("검색 조건에 따른 피드 목록을 페이징으로 조회할 수 있다")
    void findWithSearchConditions() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        FeedSearchRequest searchRequest = FeedSearchRequest.builder()
                .keyword("테스트")
                .build();
        Page<Feed> feedPage = new PageImpl<>(Arrays.asList(testFeed), pageable, 1);
        given(feedQueryRepository.findWithSearchConditions(any(FeedSearchRequest.class), eq(pageable))).willReturn(feedPage);

        // when
        Page<Feed> result = feedRepository.findWithSearchConditions(searchRequest, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testFeed);
        verify(feedQueryRepository, times(1)).findWithSearchConditions(any(FeedSearchRequest.class), eq(pageable));
    }

    @Test
    @DisplayName("피드에 이미지를 추가할 수 있다")
    void addImageToFeed() {
        // given
        FeedImage image = FeedImage.builder()
                .feed(testFeed)
                .imageUrl("test-image.jpg")
                .sortOrder(1)
                .build();

        // when
        testFeed.addImage("test-image.jpg", 1);

        // then
        assertThat(testFeed.getImages()).hasSize(1);
        assertThat(testFeed.getImages().get(0).getImageUrl()).isEqualTo("test-image.jpg");
        assertThat(testFeed.getImages().get(0).getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("피드에 해시태그를 추가할 수 있다")
    void addHashtagToFeed() {
        // given
        String hashtag = "테스트";

        // when
        testFeed.addHashtag(hashtag);

        // then
        assertThat(testFeed.getHashtags()).hasSize(1);
        assertThat(testFeed.getHashtags().get(0).getTag()).isEqualTo(hashtag);
    }

    @Test
    @DisplayName("피드에 여러 해시태그를 일괄 추가할 수 있다")
    void addMultipleHashtagsToFeed() {
        // given
        List<String> hashtags = Arrays.asList("테스트", "피드", "좋아요");

        // when
        testFeed.addHashtags(hashtags);

        // then
        assertThat(testFeed.getHashtags()).hasSize(3);
        assertThat(testFeed.getHashtags().get(0).getTag()).isEqualTo("테스트");
        assertThat(testFeed.getHashtags().get(1).getTag()).isEqualTo("피드");
        assertThat(testFeed.getHashtags().get(2).getTag()).isEqualTo("좋아요");
    }

    @Test
    @DisplayName("피드의 좋아요 수를 증가시킬 수 있다")
    void incrementLikeCount() {
        // given
        int initialLikeCount = testFeed.getLikeCount();

        // when
        testFeed.incrementLikeCount();

        // then
        assertThat(testFeed.getLikeCount()).isEqualTo(initialLikeCount + 1);
    }

    @Test
    @DisplayName("피드의 좋아요 수를 감소시킬 수 있다")
    void decrementLikeCount() {
        // given
        testFeed.incrementLikeCount(); // 먼저 증가
        int currentLikeCount = testFeed.getLikeCount();

        // when
        testFeed.decrementLikeCount();

        // then
        assertThat(testFeed.getLikeCount()).isEqualTo(currentLikeCount - 1);
    }

    @Test
    @DisplayName("피드의 댓글 수를 증가시킬 수 있다")
    void incrementCommentCount() {
        // given
        int initialCommentCount = testFeed.getCommentCount();

        // when
        testFeed.incrementCommentCount();

        // then
        assertThat(testFeed.getCommentCount()).isEqualTo(initialCommentCount + 1);
    }

    @Test
    @DisplayName("피드의 투표 수를 증가시킬 수 있다")
    void incrementVoteCount() {
        // given
        int initialVoteCount = testFeed.getParticipantVoteCount();

        // when
        testFeed.incrementVoteCount();

        // then
        assertThat(testFeed.getParticipantVoteCount()).isEqualTo(initialVoteCount + 1);
    }

    @Test
    @DisplayName("피드 내용을 업데이트할 수 있다")
    void updateContent() {
        // given
        String newTitle = "업데이트된 제목";
        String newContent = "업데이트된 내용";
        String newInstagramId = "updated_instagram";

        // when
        testFeed.updateContent(newTitle, newContent, newInstagramId);

        // then
        assertThat(testFeed.getTitle()).isEqualTo(newTitle);
        assertThat(testFeed.getContent()).isEqualTo(newContent);
        assertThat(testFeed.getInstagramId()).isEqualTo(newInstagramId);
    }

    @Test
    @DisplayName("피드가 삭제되었는지 확인할 수 있다")
    void isDeleted() {
        // given
        assertThat(testFeed.isDeleted()).isFalse();

        // when
        testFeed.softDelete();

        // then
        assertThat(testFeed.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("피드가 조회 가능한지 확인할 수 있다")
    void isViewable() {
        // given
        assertThat(testFeed.isViewable()).isTrue();

        // when
        testFeed.softDelete();

        // then
        assertThat(testFeed.isViewable()).isFalse();
    }

    @Test
    @DisplayName("이벤트 피드인지 확인할 수 있다")
    void isEventFeed() {
        // given
        Feed eventFeed = Feed.builder()
                .event(testEvent)
                .orderItem(testOrderItem)
                .user(testUser)
                .title("이벤트 피드")
                .content("이벤트 피드 내용")
                .build();

        // when & then
        assertThat(eventFeed.isEventFeed()).isTrue();
        assertThat(testFeed.isEventFeed()).isTrue(); // testFeed도 이벤트가 있음
    }

    @Test
    @DisplayName("데일리 피드인지 확인할 수 있다")
    void isDailyFeed() {
        // given
        Feed dailyFeed = Feed.builder()
                .orderItem(testOrderItem)
                .user(testUser)
                .title("데일리 피드")
                .content("데일리 피드 내용")
                .build();

        // when & then
        assertThat(dailyFeed.isDailyFeed()).isTrue();
    }
}
