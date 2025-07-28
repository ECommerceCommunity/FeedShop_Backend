package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewRepositoryImpl 테스트")
class ReviewRepositoryImplTest {

    @Mock
    private ReviewJpaRepository reviewJpaRepository;

    @InjectMocks
    private ReviewRepositoryImpl reviewRepository;

    private Review testReview;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testLogin", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testReview = Review.builder()
                .title("좋은 신발입니다")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .content("정말 편하고 좋습니다. 추천해요!")
                .user(testUser)
                .productId(1L)
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
    @DisplayName("상품별 활성 리뷰를 페이징으로 조회할 수 있다")
    void findActiveReviewsByProductId() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview), pageable, 1);
        given(reviewJpaRepository.findActiveReviewsByProductId(1L, pageable))
                .willReturn(reviewPage);

        // when
        Page<Review> result = reviewRepository.findActiveReviewsByProductId(1L, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testReview);
        verify(reviewJpaRepository, times(1)).findActiveReviewsByProductId(1L, pageable);
    }

    @Test
    @DisplayName("상품별 평균 평점을 조회할 수 있다")
    void findAverageRatingByProductId() {
        // given
        given(reviewJpaRepository.findAverageRatingByProductId(1L)).willReturn(4.5);

        // when
        Double averageRating = reviewRepository.findAverageRatingByProductId(1L);

        // then
        assertThat(averageRating).isEqualTo(4.5);
        verify(reviewJpaRepository, times(1)).findAverageRatingByProductId(1L);
    }

    @Test
    @DisplayName("상품별 리뷰 개수를 조회할 수 있다")
    void countActiveReviewsByProductId() {
        // given
        given(reviewJpaRepository.countActiveReviewsByProductId(1L)).willReturn(10L);

        // when
        Long count = reviewRepository.countActiveReviewsByProductId(1L);

        // then
        assertThat(count).isEqualTo(10L);
        verify(reviewJpaRepository, times(1)).countActiveReviewsByProductId(1L);
    }
}