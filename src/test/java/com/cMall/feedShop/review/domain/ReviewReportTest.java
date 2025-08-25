package com.cMall.feedShop.review.domain;

import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.review.domain.enums.ReportReason;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReviewReport 도메인 테스트")
class ReviewReportTest {

    private Review review;
    private User reporter;
    private User reviewer;
    private Product product;

    @BeforeEach
    void setUp() {
        Store store = Store.builder()
                .storeName("Test Store")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(store, "storeId", 1L);

        Category category = new Category();
        ReflectionTestUtils.setField(category, "categoryId", 1L);

        product = Product.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(10000))
                .store(store)
                .category(category)
                .build();
        ReflectionTestUtils.setField(product, "productId", 1L);

        reviewer = User.builder()
                .loginId("reviewer")
                .password("password")
                .email("reviewer@test.com")
                .role(com.cMall.feedShop.user.domain.enums.UserRole.USER)
                .build();
        ReflectionTestUtils.setField(reviewer, "id", 1L);

        review = Review.builder()
                .user(reviewer)
                .product(product)
                .title("Test Review")
                .content("Test Content")
                .rating(5)
                .sizeFit(com.cMall.feedShop.review.domain.enums.SizeFit.NORMAL)
                .cushion(com.cMall.feedShop.review.domain.enums.Cushion.SOFT)
                .stability(com.cMall.feedShop.review.domain.enums.Stability.STABLE)
                .build();
        ReflectionTestUtils.setField(review, "reviewId", 1L);

        reporter = User.builder()
                .loginId("reporter")
                .password("password")
                .email("reporter@test.com")
                .role(com.cMall.feedShop.user.domain.enums.UserRole.USER)
                .build();
        ReflectionTestUtils.setField(reporter, "id", 2L);
    }

    @Test
    @DisplayName("리뷰 신고 생성 성공")
    void createReviewReport_Success() {
        // given
        ReportReason reason = ReportReason.ABUSIVE_LANGUAGE;
        String description = "욕설이 포함되어 있습니다.";

        // when
        ReviewReport reviewReport = ReviewReport.builder()
                .review(review)
                .reporter(reporter)
                .reason(reason)
                .description(description)
                .build();

        // then
        assertThat(reviewReport.getReview()).isEqualTo(review);
        assertThat(reviewReport.getReporter()).isEqualTo(reporter);
        assertThat(reviewReport.getReason()).isEqualTo(reason);
        assertThat(reviewReport.getDescription()).isEqualTo(description);
        assertThat(reviewReport.isProcessed()).isFalse();
    }

    @Test
    @DisplayName("리뷰가 null일 때 예외 발생")
    void createReviewReport_NullReview_ThrowsException() {
        // given
        ReportReason reason = ReportReason.ABUSIVE_LANGUAGE;
        String description = "욕설이 포함되어 있습니다.";

        // when & then
        assertThatThrownBy(() -> ReviewReport.builder()
                .review(null)
                .reporter(reporter)
                .reason(reason)
                .description(description)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("신고할 리뷰는 필수입니다.");
    }

    @Test
    @DisplayName("신고자가 null일 때 예외 발생")
    void createReviewReport_NullReporter_ThrowsException() {
        // given
        ReportReason reason = ReportReason.ABUSIVE_LANGUAGE;
        String description = "욕설이 포함되어 있습니다.";

        // when & then
        assertThatThrownBy(() -> ReviewReport.builder()
                .review(review)
                .reporter(null)
                .reason(reason)
                .description(description)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("신고자 정보는 필수입니다.");
    }

    @Test
    @DisplayName("신고 사유가 null일 때 예외 발생")
    void createReviewReport_NullReason_ThrowsException() {
        // given
        String description = "욕설이 포함되어 있습니다.";

        // when & then
        assertThatThrownBy(() -> ReviewReport.builder()
                .review(review)
                .reporter(reporter)
                .reason(null)
                .description(description)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("신고 사유는 필수입니다.");
    }

    @Test
    @DisplayName("자신이 작성한 리뷰 신고 시 예외 발생")
    void createReviewReport_SelfReport_ThrowsException() {
        // given
        ReportReason reason = ReportReason.ABUSIVE_LANGUAGE;
        String description = "욕설이 포함되어 있습니다.";
        
        Review selfReview = Review.builder()
                .user(reporter)
                .product(product)
                .title("Self Review")
                .content("Self Content")
                .rating(5)
                .sizeFit(com.cMall.feedShop.review.domain.enums.SizeFit.NORMAL)
                .cushion(com.cMall.feedShop.review.domain.enums.Cushion.SOFT)
                .stability(com.cMall.feedShop.review.domain.enums.Stability.STABLE)
                .build();
        ReflectionTestUtils.setField(selfReview, "reviewId", 2L);

        // when & then
        assertThatThrownBy(() -> ReviewReport.builder()
                .review(selfReview)
                .reporter(reporter)
                .reason(reason)
                .description(description)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("자신이 작성한 리뷰는 신고할 수 없습니다.");
    }

    @Test
    @DisplayName("신고 처리 완료 표시")
    void markAsProcessed_Success() {
        // given
        ReviewReport reviewReport = ReviewReport.builder()
                .review(review)
                .reporter(reporter)
                .reason(ReportReason.ABUSIVE_LANGUAGE)
                .description("욕설이 포함되어 있습니다.")
                .build();

        assertThat(reviewReport.isProcessed()).isFalse();

        // when
        reviewReport.markAsProcessed();

        // then
        assertThat(reviewReport.isProcessed()).isTrue();
    }

    @Test
    @DisplayName("설명이 없어도 신고 생성 가능")
    void createReviewReport_WithoutDescription_Success() {
        // given
        ReportReason reason = ReportReason.SPAM;

        // when
        ReviewReport reviewReport = ReviewReport.builder()
                .review(review)
                .reporter(reporter)
                .reason(reason)
                .description(null)
                .build();

        // then
        assertThat(reviewReport.getReview()).isEqualTo(review);
        assertThat(reviewReport.getReporter()).isEqualTo(reporter);
        assertThat(reviewReport.getReason()).isEqualTo(reason);
        assertThat(reviewReport.getDescription()).isNull();
        assertThat(reviewReport.isProcessed()).isFalse();
    }

    @Test
    @DisplayName("다양한 신고 사유로 신고 생성 가능")
    void createReviewReport_WithDifferentReasons_Success() {
        // given
        ReportReason[] reasons = {
                ReportReason.ABUSIVE_LANGUAGE,
                ReportReason.SPAM,
                ReportReason.INAPPROPRIATE_CONTENT,
                ReportReason.FALSE_INFORMATION,
                ReportReason.ADVERTISING,
                ReportReason.COPYRIGHT_VIOLATION,
                ReportReason.OTHER
        };

        for (ReportReason reason : reasons) {
            // when
            ReviewReport reviewReport = ReviewReport.builder()
                    .review(review)
                    .reporter(reporter)
                    .reason(reason)
                    .description("신고 설명")
                    .build();

            // then
            assertThat(reviewReport.getReason()).isEqualTo(reason);
            assertThat(reviewReport.isProcessed()).isFalse();
        }
    }
}