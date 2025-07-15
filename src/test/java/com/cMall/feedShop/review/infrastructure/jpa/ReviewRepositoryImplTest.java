package com.cMall.feedShop.review.infrastructure.jpa;

import com.cMall.feedShop.review.domain.entity.Review;
import com.cMall.feedShop.review.domain.entity.SizeFit;
import com.cMall.feedShop.review.domain.entity.Cushion;
import com.cMall.feedShop.review.domain.entity.Stability;
import com.cMall.feedShop.review.domain.entity.ReviewStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Optional;

@DataJpaTest
class ReviewRepositoryImplTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewJpaRepository reviewJpaRepository;

    @Test
    @DisplayName("Given shoe review with 5-level characteristics_When save_Then persist all levels correctly")
    void givenShoeReviewWith5LevelCharacteristics_whenSave_thenPersistAllLevelsCorrectly() {
        // given
        Review extremeReview = Review.builder()
                .content("극단적인 특성의 신발 테스트")
                .rating(1)
                .userId(1L)
                .productId(1L)
                .sizeFit(SizeFit.VERY_SMALL)      // 매우 작음
                .cushioning(Cushion.VERY_FIRM)    // 매우 단단함
                .stability(Stability.VERY_UNSTABLE) // 매우 불안정
                .status(ReviewStatus.ACTIVE)
                .build();

        // when
        Review savedReview = reviewJpaRepository.save(extremeReview);
        entityManager.flush();

        // then
        assertNotNull(savedReview.getId());
        assertEquals(SizeFit.VERY_SMALL, savedReview.getSizeFit());
        assertEquals(Cushion.VERY_FIRM, savedReview.getCushioning());
        assertEquals(Stability.VERY_UNSTABLE, savedReview.getStability());
    }
/*
    @Test
    @DisplayName("Given various size fit levels_When find by each level_Then return accurate filtering")
    void givenVariousSizeFitLevels_whenFindByEachLevel_thenReturnAccurateFiltering() {
        // given - 5가지 사이즈 핏 모두 생성
        Long productId = 1L;
        createShoeReviewWithSizeFit("매우 작은 신발", productId, SizeFit.VERY_SMALL);
        createShoeReviewWithSizeFit("작은 신발", productId, SizeFit.SMALL);
        createShoeReviewWithSizeFit("딱 맞는 신발", productId, SizeFit.PERFECT);
        createShoeReviewWithSizeFit("큰 신발", productId, SizeFit.BIG);
        createShoeReviewWithSizeFit("매우 큰 신발", productId, SizeFit.VERY_BIG);

        // when & then - 각 레벨별로 정확히 조회되는지 확인
        List<Review> verySmallReviews = reviewJpaRepository.findByProductIdAndSizeFitAndStatus(
                productId, SizeFit.VERY_SMALL, ReviewStatus.ACTIVE);
        assertEquals(1, verySmallReviews.size());
        assertTrue(verySmallReviews.get(0).getContent().contains("매우 작은"));

        List<Review> perfectReviews = reviewJpaRepository.findByProductIdAndSizeFitAndStatus(
                productId, SizeFit.PERFECT, ReviewStatus.ACTIVE);
        assertEquals(1, perfectReviews.size());
        assertTrue(perfectReviews.get(0).getContent().contains("딱 맞는"));

        List<Review> veryBigReviews = reviewJpaRepository.findByProductIdAndSizeFitAndStatus(
                productId, SizeFit.VERY_BIG, ReviewStatus.ACTIVE);
        assertEquals(1, veryBigReviews.size());
        assertTrue(veryBigReviews.get(0).getContent().contains("매우 큰"));
    }

    @Test
    @DisplayName("Given various cushioning levels_When find by extreme levels_Then return matching reviews")
    void givenVariousCushioningLevels_whenFindByExtremeLevels_thenReturnMatchingReviews() {
        // given - 극단적인 쿠션 레벨들
        Long productId = 1L;
        createShoeReviewWithCushioning("구름같은 쿠션", productId, Cushion.VERY_SOFT);
        createShoeReviewWithCushioning("바위같은 쿠션", productId, Cushion.VERY_FIRM);
        createShoeReviewWithCushioning("적당한 쿠션", productId, Cushion.NORMAL);

        // when & then - 매우 부드러운 쿠션만 조회
        List<Review> verySoftReviews = reviewJpaRepository.findByProductIdAndCushioningAndStatus(
                productId, Cushion.VERY_SOFT, ReviewStatus.ACTIVE);
        assertEquals(1, verySoftReviews.size());
        assertTrue(verySoftReviews.get(0).getContent().contains("구름같은"));

        // when & then - 매우 단단한 쿠션만 조회
        List<Review> veryFirmReviews = reviewJpaRepository.findByProductIdAndCushioningAndStatus(
                productId, Cushion.VERY_FIRM, ReviewStatus.ACTIVE);
        assertEquals(1, veryFirmReviews.size());
        assertTrue(veryFirmReviews.get(0).getContent().contains("바위같은"));
    }

    @Test
    @DisplayName("Given stability extremes_When find by very stable and very unstable_Then return appropriate reviews")
    void givenStabilityExtremes_whenFindByVeryStableAndVeryUnstable_thenReturnAppropriateReviews() {
        // given
        Long productId = 1L;
        createShoeReviewWithStability("발목 완전 고정", productId, Stability.VERY_STABLE);
        createShoeReviewWithStability("발목 완전 불안", productId, Stability.VERY_UNSTABLE);
        createShoeReviewWithStability("보통 지지력", productId, Stability.NORMAL);

        // when & then - 매우 안정적인 것만 조회
        List<Review> veryStableReviews = reviewJpaRepository.findByProductIdAndStabilityAndStatus(
                productId, Stability.VERY_STABLE, ReviewStatus.ACTIVE);
        assertEquals(1, veryStableReviews.size());
        assertTrue(veryStableReviews.get(0).getContent().contains("완전 고정"));

        // when & then - 매우 불안정한 것만 조회
        List<Review> veryUnstableReviews = reviewJpaRepository.findByProductIdAndStabilityAndStatus(
                productId, Stability.VERY_UNSTABLE, ReviewStatus.ACTIVE);
        assertEquals(1, veryUnstableReviews.size());
        assertTrue(veryUnstableReviews.get(0).getContent().contains("완전 불안"));
    }
*/
    private Review createShoeReviewWithSizeFit(String content, Long productId, SizeFit sizeFit) {
        Review review = Review.builder()
                .content(content)
                .rating(3)
                .userId(1L)
                .productId(productId)
                .sizeFit(sizeFit)
                .cushioning(Cushion.NORMAL)
                .stability(Stability.NORMAL)
                .status(ReviewStatus.ACTIVE)
                .build();
        return reviewJpaRepository.save(review);
    }

    private Review createShoeReviewWithCushioning(String content, Long productId, Cushion cushioning) {
        Review review = Review.builder()
                .content(content)
                .rating(3)
                .userId(1L)
                .productId(productId)
                .sizeFit(SizeFit.PERFECT)
                .cushioning(cushioning)
                .stability(Stability.NORMAL)
                .status(ReviewStatus.ACTIVE)
                .build();
        return reviewJpaRepository.save(review);
    }

    private Review createShoeReviewWithStability(String content, Long productId, Stability stability) {
        Review review = Review.builder()
                .content(content)
                .rating(3)
                .userId(1L)
                .productId(productId)
                .sizeFit(SizeFit.PERFECT)
                .cushioning(Cushion.NORMAL)
                .stability(stability)
                .status(ReviewStatus.ACTIVE)
                .build();
        return reviewJpaRepository.save(review);
    }
}