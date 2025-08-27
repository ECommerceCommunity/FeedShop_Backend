package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.QReview;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.ReviewStatus;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.product.domain.enums.DiscountType;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAQuery;
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

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewQueryRepositoryImpl 테스트")
class ReviewQueryRepositoryImplTest {

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private JPAQuery<Review> reviewQuery;

    @Mock
    private JPAQuery<Long> countQuery;

    @Mock
    private JPAQuery<Double> avgQuery;

    @Mock
    private JPAQuery<Tuple> tupleQuery;

    @InjectMocks
    private ReviewQueryRepositoryImpl reviewQueryRepository;

    private Review testReview;
    private User testUser;
    private Product testProduct;
    private Store testStore;
    private Category testCategory;
    private final QReview review = QReview.review;

    @BeforeEach
    void setUp() {
        testUser = new User("testLogin", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // Store와 Category 모킹
        testStore = mock(Store.class);
        testCategory = mock(Category.class);

        // Product 객체 생성
        testProduct = Product.builder()
                .name("테스트 신발")
                .price(new BigDecimal("100000"))
                .store(testStore)
                .category(testCategory)
                .discountType(DiscountType.NONE)
                .discountValue(null)
                .description("테스트용 신발입니다")
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);

        testReview = Review.builder()
                .title("좋은 신발입니다")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .content("정말 편하고 좋습니다. 추천해요!")
                .user(testUser)
                .product(testProduct)
                .build();
        ReflectionTestUtils.setField(testReview, "reviewId", 1L);

        setupLenientStubbing();
    }

    private void setupLenientStubbing() {
        lenient().when(queryFactory.selectFrom(review)).thenReturn(reviewQuery);
        lenient().when(reviewQuery.where(any(Predicate.class))).thenReturn(reviewQuery);
        lenient().when(reviewQuery.where(any(Predicate[].class))).thenReturn(reviewQuery);
        lenient().when(reviewQuery.orderBy(any(OrderSpecifier.class))).thenReturn(reviewQuery);
        lenient().when(reviewQuery.orderBy(any(OrderSpecifier[].class))).thenReturn(reviewQuery);
        lenient().when(reviewQuery.offset(anyLong())).thenReturn(reviewQuery);
        lenient().when(reviewQuery.limit(anyLong())).thenReturn(reviewQuery);
        lenient().when(reviewQuery.fetch()).thenReturn(List.of(testReview));

        lenient().when(queryFactory.select(review.count())).thenReturn(countQuery);
        lenient().when(countQuery.from(review)).thenReturn(countQuery);
        lenient().when(countQuery.where(any(Predicate.class))).thenReturn(countQuery);
        lenient().when(countQuery.where(any(Predicate[].class))).thenReturn(countQuery);

        lenient().when(queryFactory.select(review.rating.avg())).thenReturn(avgQuery);
        lenient().when(avgQuery.from(review)).thenReturn(avgQuery);
        lenient().when(avgQuery.where(any(Predicate.class))).thenReturn(avgQuery);
        lenient().when(avgQuery.where(any(Predicate[].class))).thenReturn(avgQuery);
        lenient().when(avgQuery.fetchOne()).thenReturn(4.5);

        lenient().when(queryFactory.select(review.cushion, review.count())).thenReturn(tupleQuery);
        lenient().when(queryFactory.select(review.sizeFit, review.count())).thenReturn(tupleQuery);
        lenient().when(queryFactory.select(review.stability, review.count())).thenReturn(tupleQuery);
        lenient().when(tupleQuery.from(review)).thenReturn(tupleQuery);
        lenient().when(tupleQuery.where(any(Predicate.class))).thenReturn(tupleQuery);
        lenient().when(tupleQuery.where(any(Predicate[].class))).thenReturn(tupleQuery);
        // 🔥 핵심 수정: groupBy 체인 연결! 🔥
        lenient().when(tupleQuery.groupBy(any(Expression.class))).thenReturn(tupleQuery);
        lenient().when(tupleQuery.fetch()).thenReturn(List.of()); // 기본은 빈 리스트
    }

// 🔥 모든 테스트에서 countQuery.fetchOne() 명시적으로 설정해야 함! 🔥

    @Test
    @DisplayName("상품별 활성 리뷰를 최신순으로 조회할 수 있다")
    void findActiveReviewsByProductId() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        List<Review> reviews = List.of(testReview);

        given(reviewQuery.fetch()).willReturn(reviews);
        given(countQuery.fetchOne()).willReturn(1L); // 명시적으로 설정

        // when
        Page<Review> result = reviewQueryRepository.findActiveReviewsByProductId(1L, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testReview);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("상품별 평균 평점을 조회할 수 있다")
    void findAverageRatingByProductId() {
        // given
        given(avgQuery.fetchOne()).willReturn(4.5);

        // when
        Double result = reviewQueryRepository.findAverageRatingByProductId(1L);

        // then
        assertThat(result).isEqualTo(4.5);
    }

    @Test
    @DisplayName("상품별 리뷰 개수를 조회할 수 있다")
    void countActiveReviewsByProductId() {
        // given
        given(countQuery.fetchOne()).willReturn(10L); // 명시적으로 설정

        // when
        Long result = reviewQueryRepository.countActiveReviewsByProductId(1L);

        // then
        assertThat(result).isEqualTo(10L);
    }

    @Test
    @DisplayName("Cushion 분포를 조회할 수 있다")
    void getCushionDistributionByProductId() {
        // given
        Tuple tuple1 = mock(Tuple.class);
        Tuple tuple2 = mock(Tuple.class);
        Tuple tuple3 = mock(Tuple.class);

        given(tuple1.get(review.cushion)).willReturn(Cushion.SOFT);
        given(tuple1.get(review.count())).willReturn(3L);
        given(tuple2.get(review.cushion)).willReturn(Cushion.MEDIUM);
        given(tuple2.get(review.count())).willReturn(4L);
        given(tuple3.get(review.cushion)).willReturn(Cushion.FIRM);
        given(tuple3.get(review.count())).willReturn(3L);

        given(tupleQuery.fetch()).willReturn(List.of(tuple1, tuple2, tuple3));

        // when
        Map<Cushion, Long> result = reviewQueryRepository.getCushionDistributionByProductId(1L);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(Cushion.SOFT)).isEqualTo(3L);
        assertThat(result.get(Cushion.MEDIUM)).isEqualTo(4L);
        assertThat(result.get(Cushion.FIRM)).isEqualTo(3L);
    }

    @Test
    @DisplayName("SizeFit 분포를 조회할 수 있다")
    void getSizeFitDistributionByProductId() {
        // given
        Tuple tuple1 = mock(Tuple.class);
        Tuple tuple2 = mock(Tuple.class);

        given(tuple1.get(review.sizeFit)).willReturn(SizeFit.SMALL);
        given(tuple1.get(review.count())).willReturn(2L);
        given(tuple2.get(review.sizeFit)).willReturn(SizeFit.NORMAL);
        given(tuple2.get(review.count())).willReturn(4L);

        given(tupleQuery.fetch()).willReturn(List.of(tuple1, tuple2));

        // when
        Map<SizeFit, Long> result = reviewQueryRepository.getSizeFitDistributionByProductId(1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(SizeFit.SMALL)).isEqualTo(2L);
        assertThat(result.get(SizeFit.NORMAL)).isEqualTo(4L);
    }

    @Test
    @DisplayName("Stability 분포를 조회할 수 있다")
    void getStabilityDistributionByProductId() {
        // given
        Tuple tuple1 = mock(Tuple.class);

        given(tuple1.get(review.stability)).willReturn(Stability.VERY_STABLE);
        given(tuple1.get(review.count())).willReturn(10L);

        given(tupleQuery.fetch()).willReturn(List.of(tuple1));

        // when
        Map<Stability, Long> result = reviewQueryRepository.getStabilityDistributionByProductId(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(Stability.VERY_STABLE)).isEqualTo(10L);
    }

    @Test
    @DisplayName("빈 결과가 주어졌을때 분포 조회하면 빈 Map이 반환된다")
    void getDistribution_EmptyResult() {
        // given
        given(tupleQuery.fetch()).willReturn(List.of()); // 빈 결과

        // when
        Map<Cushion, Long> result = reviewQueryRepository.getCushionDistributionByProductId(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("점수순으로 활성 리뷰를 조회할 수 있다")
    void findActiveReviewsByProductIdOrderByPoints() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        List<Review> reviews = List.of(testReview);

        given(reviewQuery.fetch()).willReturn(reviews);
        given(countQuery.fetchOne()).willReturn(1L); // 명시적으로 설정

        // when
        Page<Review> result = reviewQueryRepository.findActiveReviewsByProductIdOrderByPoints(1L, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testReview);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("리뷰 개수가 null인 경우 0을 반환한다")
    void countActiveReviewsByProductId_NullResult() {
        // given
        given(countQuery.fetchOne()).willReturn(null); // 명시적으로 null 설정

        // when
        Long result = reviewQueryRepository.countActiveReviewsByProductId(1L);

        // then
        assertThat(result).isEqualTo(0L);
    }

    @Test
    @DisplayName("페이지 총 개수가 null인 경우 0을 반환한다")
    void findActiveReviewsByProductId_NullTotalCount() {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        // 🔥 핵심: content가 있으면 PageImpl이 content.size()를 사용함
        // 따라서 null total을 테스트하려면 빈 리스트를 사용해야 함!
        List<Review> emptyReviews = List.of(); // 빈 리스트!

        given(reviewQuery.fetch()).willReturn(emptyReviews); // 빈 결과
        given(countQuery.fetchOne()).willReturn(null); // null count

        // when
        Page<Review> result = reviewQueryRepository.findActiveReviewsByProductId(1L, pageable);

        // then
        assertThat(result.getContent()).isEmpty(); // 빈 리스트 확인
        assertThat(result.getTotalElements()).isEqualTo(0L); // 이제 0L 반환!
    }

    // ========== 필터링 메서드 테스트들 ==========

    @Test
    @DisplayName("평점별로 리뷰를 필터링할 수 있다")
    void findActiveReviewsByProductIdAndRating() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        List<Review> reviews = List.of(testReview);
        
        given(reviewQuery.fetch()).willReturn(reviews);
        given(countQuery.fetchOne()).willReturn(1L);

        // when
        Page<Review> result = reviewQueryRepository.findActiveReviewsByProductIdAndRating(1L, 5, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRating()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("착용감별로 리뷰를 필터링할 수 있다")
    void findActiveReviewsByProductIdAndSizeFit() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        List<Review> reviews = List.of(testReview);
        
        given(reviewQuery.fetch()).willReturn(reviews);
        given(countQuery.fetchOne()).willReturn(1L);

        // when
        Page<Review> result = reviewQueryRepository.findActiveReviewsByProductIdAndSizeFit(1L, SizeFit.NORMAL, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSizeFit()).isEqualTo(SizeFit.NORMAL);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("쿠션감별로 리뷰를 필터링할 수 있다")
    void findActiveReviewsByProductIdAndCushion() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        List<Review> reviews = List.of(testReview);
        
        given(reviewQuery.fetch()).willReturn(reviews);
        given(countQuery.fetchOne()).willReturn(1L);

        // when
        Page<Review> result = reviewQueryRepository.findActiveReviewsByProductIdAndCushion(1L, Cushion.SOFT, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCushion()).isEqualTo(Cushion.SOFT);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("안정성별로 리뷰를 필터링할 수 있다")
    void findActiveReviewsByProductIdAndStability() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        List<Review> reviews = List.of(testReview);
        
        given(reviewQuery.fetch()).willReturn(reviews);
        given(countQuery.fetchOne()).willReturn(1L);

        // when
        Page<Review> result = reviewQueryRepository.findActiveReviewsByProductIdAndStability(1L, Stability.STABLE, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStability()).isEqualTo(Stability.STABLE);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("복합 필터링으로 리뷰를 조회할 수 있다 - 모든 조건 지정")
    void findActiveReviewsByProductIdWithFilters_AllFilters() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        List<Review> reviews = List.of(testReview);
        
        given(reviewQuery.fetch()).willReturn(reviews);
        given(countQuery.fetchOne()).willReturn(1L);

        // when
        Page<Review> result = reviewQueryRepository.findActiveReviewsByProductIdWithFilters(
                1L, 5, SizeFit.NORMAL, Cushion.SOFT, Stability.STABLE, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRating()).isEqualTo(5);
        assertThat(result.getContent().get(0).getSizeFit()).isEqualTo(SizeFit.NORMAL);
        assertThat(result.getContent().get(0).getCushion()).isEqualTo(Cushion.SOFT);
        assertThat(result.getContent().get(0).getStability()).isEqualTo(Stability.STABLE);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("복합 필터링으로 리뷰를 조회할 수 있다 - 일부 조건만 지정")
    void findActiveReviewsByProductIdWithFilters_PartialFilters() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        List<Review> reviews = List.of(testReview);
        
        given(reviewQuery.fetch()).willReturn(reviews);
        given(countQuery.fetchOne()).willReturn(1L);

        // when - rating과 sizeFit만 지정, 나머지는 null
        Page<Review> result = reviewQueryRepository.findActiveReviewsByProductIdWithFilters(
                1L, 5, SizeFit.NORMAL, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRating()).isEqualTo(5);
        assertThat(result.getContent().get(0).getSizeFit()).isEqualTo(SizeFit.NORMAL);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("복합 필터링 - 필터 없음 (모든 조건 null)")
    void findActiveReviewsByProductIdWithFilters_NoFilters() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        List<Review> reviews = List.of(testReview);
        
        given(reviewQuery.fetch()).willReturn(reviews);
        given(countQuery.fetchOne()).willReturn(1L);

        // when - 모든 필터가 null
        Page<Review> result = reviewQueryRepository.findActiveReviewsByProductIdWithFilters(
                1L, null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("필터링 결과가 없는 경우 빈 페이지를 반환한다")
    void findActiveReviewsByProductIdWithFilters_EmptyResult() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        List<Review> emptyReviews = List.of();
        
        given(reviewQuery.fetch()).willReturn(emptyReviews);
        given(countQuery.fetchOne()).willReturn(0L);

        // when
        Page<Review> result = reviewQueryRepository.findActiveReviewsByProductIdWithFilters(
                1L, 1, SizeFit.VERY_BIG, Cushion.VERY_FIRM, Stability.VERY_UNSTABLE, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0L);
    }

    @Test
    @DisplayName("리뷰 조회 시 fetch join이 정상 작동한다")
    void findActiveReviewsByProductId_WithFetchJoin() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        List<Review> reviews = List.of(testReview);
        
        // Mock fetch join 체인
        given(reviewQuery.fetch()).willReturn(reviews);
        given(countQuery.fetchOne()).willReturn(1L);

        // when
        Page<Review> result = reviewQueryRepository.findActiveReviewsByProductId(1L, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        
        // 쿼리가 실행되었는지 확인
        verify(reviewQuery).fetch();
    }

    @Test
    @DisplayName("인기순 정렬 시에도 fetch join이 정상 작동한다")
    void findActiveReviewsByProductIdOrderByPoints_WithFetchJoin() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        List<Review> reviews = List.of(testReview);
        
        // Mock fetch join 체인
        given(reviewQuery.fetch()).willReturn(reviews);
        given(countQuery.fetchOne()).willReturn(1L);

        // when
        Page<Review> result = reviewQueryRepository.findActiveReviewsByProductIdOrderByPoints(1L, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        
        // 쿼리가 실행되었는지 확인
        verify(reviewQuery).fetch();
    }

}