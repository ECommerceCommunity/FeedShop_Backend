package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.product.domain.enums.DiscountType;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewRepositoryImpl 테스트")
class ReviewRepositoryImplTest {

    @Mock
    private ReviewJpaRepository reviewJpaRepository;

    @Mock
    private ReviewQueryRepository reviewQueryRepository;

    @InjectMocks
    private ReviewRepositoryImpl reviewRepository;

    private Review testReview;
    private User testUser;
    private Product testProduct;
    private Store testStore;
    private Category testCategory;

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
                .product(testProduct)  // 수정된 부분: productId(1L) → product(testProduct)
                .build();
        ReflectionTestUtils.setField(testReview, "reviewId", 1L);
    }

    @Test
    @DisplayName("리뷰를 성공적으로 저장할 수 있다")
    void saveReview() {
        // given
        given(reviewJpaRepository.save(any(Review.class))).willReturn(testReview);

        // when
        Review savedReview = reviewRepository.save(testReview);

        // then
        assertThat(savedReview).isEqualTo(testReview);
        verify(reviewJpaRepository, times(1)).save(testReview);
    }

    @Test
    @DisplayName("ID로 리뷰를 조회할 수 있다")
    void findById() {
        // given
        given(reviewJpaRepository.findById(1L)).willReturn(Optional.of(testReview));

        // when
        Optional<Review> foundReview = reviewRepository.findById(1L);

        // then
        assertThat(foundReview).isPresent();
        assertThat(foundReview.get()).isEqualTo(testReview);
        verify(reviewJpaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("리뷰를 삭제할 수 있다")
    void deleteReview() {
        // when
        reviewRepository.delete(testReview);

        // then
        verify(reviewJpaRepository, times(1)).delete(testReview);
    }

    @Test
    @DisplayName("상품별 활성 리뷰를 페이징으로 조회할 수 있다")
    void findActiveReviewsByProductId() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview), pageable, 1);
        given(reviewQueryRepository.findActiveReviewsByProductId(1L, pageable))
                .willReturn(reviewPage);

        // when
        Page<Review> result = reviewRepository.findActiveReviewsByProductId(1L, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testReview);
        verify(reviewQueryRepository, times(1)).findActiveReviewsByProductId(1L, pageable);
    }

    @Test
    @DisplayName("상품별 활성 리뷰를 점수순으로 조회할 수 있다")
    void findActiveReviewsByProductIdOrderByPoints() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview), pageable, 1);
        given(reviewQueryRepository.findActiveReviewsByProductIdOrderByPoints(1L, pageable))
                .willReturn(reviewPage);

        // when
        Page<Review> result = reviewRepository.findActiveReviewsByProductIdOrderByPoints(1L, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testReview);
        verify(reviewQueryRepository, times(1)).findActiveReviewsByProductIdOrderByPoints(1L, pageable);
    }

    @Test
    @DisplayName("상품별 평균 평점을 조회할 수 있다")
    void findAverageRatingByProductId() {
        // given
        given(reviewQueryRepository.findAverageRatingByProductId(1L)).willReturn(4.5);

        // when
        Double averageRating = reviewRepository.findAverageRatingByProductId(1L);

        // then
        assertThat(averageRating).isEqualTo(4.5);
        verify(reviewQueryRepository, times(1)).findAverageRatingByProductId(1L);
    }

    @Test
    @DisplayName("상품별 리뷰 개수를 조회할 수 있다")
    void countActiveReviewsByProductId() {
        // given
        given(reviewQueryRepository.countActiveReviewsByProductId(1L)).willReturn(10L);

        // when
        Long count = reviewRepository.countActiveReviewsByProductId(1L);

        // then
        assertThat(count).isEqualTo(10L);
        verify(reviewQueryRepository, times(1)).countActiveReviewsByProductId(1L);
    }

    @Test
    @DisplayName("Cushion 분포를 조회할 수 있다")
    void getCushionDistributionByProductId() {
        // given
        Map<Cushion, Long> expectedDistribution = Map.of(
                Cushion.SOFT, 3L,
                Cushion.MEDIUM, 4L,
                Cushion.FIRM, 3L
        );
        given(reviewQueryRepository.getCushionDistributionByProductId(1L))
                .willReturn(expectedDistribution);

        // when
        Map<Cushion, Long> result = reviewRepository.getCushionDistributionByProductId(1L);

        // then
        assertThat(result).isEqualTo(expectedDistribution);
        verify(reviewQueryRepository, times(1)).getCushionDistributionByProductId(1L);
    }

    @Test
    @DisplayName("SizeFit 분포를 조회할 수 있다")
    void getSizeFitDistributionByProductId() {
        // given
        Map<SizeFit, Long> expectedDistribution = Map.of(
                SizeFit.SMALL, 2L,
                SizeFit.NORMAL, 6L,
                SizeFit.BIG, 2L
        );
        given(reviewQueryRepository.getSizeFitDistributionByProductId(1L))
                .willReturn(expectedDistribution);

        // when
        Map<SizeFit, Long> result = reviewRepository.getSizeFitDistributionByProductId(1L);

        // then
        assertThat(result).isEqualTo(expectedDistribution);
        verify(reviewQueryRepository, times(1)).getSizeFitDistributionByProductId(1L);
    }

    @Test
    @DisplayName("Stability 분포를 조회할 수 있다")
    void getStabilityDistributionByProductId() {
        // given
        Map<Stability, Long> expectedDistribution = Map.of(
                Stability.NORMAL, 3L,
                Stability.STABLE, 5L,
                Stability.VERY_STABLE, 2L
        );
        given(reviewQueryRepository.getStabilityDistributionByProductId(1L))
                .willReturn(expectedDistribution);

        // when
        Map<Stability, Long> result = reviewRepository.getStabilityDistributionByProductId(1L);

        // then
        assertThat(result).isEqualTo(expectedDistribution);
        verify(reviewQueryRepository, times(1)).getStabilityDistributionByProductId(1L);
    }

    @Test
    @DisplayName("User와 UserProfile을 함께 조회하는 findByIdWithUserProfile이 정상 작동한다")
    void findByIdWithUserProfile() {
        // given
        Long reviewId = 1L;
        given(reviewJpaRepository.findByIdWithUserProfile(reviewId))
                .willReturn(Optional.of(testReview));

        // when
        Optional<Review> result = reviewRepository.findByIdWithUserProfile(reviewId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testReview);
        verify(reviewJpaRepository, times(1)).findByIdWithUserProfile(reviewId);
    }

    @Test
    @DisplayName("User와 UserProfile을 함께 조회할 때 리뷰가 존재하지 않으면 빈 Optional을 반환한다")
    void findByIdWithUserProfile_NotFound() {
        // given
        Long reviewId = 999L;
        given(reviewJpaRepository.findByIdWithUserProfile(reviewId))
                .willReturn(Optional.empty());

        // when
        Optional<Review> result = reviewRepository.findByIdWithUserProfile(reviewId);

        // then
        assertThat(result).isEmpty();
        verify(reviewJpaRepository, times(1)).findByIdWithUserProfile(reviewId);
    }
}