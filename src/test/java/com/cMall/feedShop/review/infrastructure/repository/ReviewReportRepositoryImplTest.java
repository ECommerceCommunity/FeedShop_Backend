package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewReport;
import com.cMall.feedShop.review.domain.enums.ReportReason;
import com.cMall.feedShop.store.domain.model.Store;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewReportRepositoryImpl 테스트")
class ReviewReportRepositoryImplTest {

    @InjectMocks
    private ReviewReportRepositoryImpl reviewReportRepository;

    @Mock
    private ReviewReportJpaRepository reviewReportJpaRepository;

    private Review review;
    private User reporter;
    private User reviewer;
    private Product product;
    private ReviewReport reviewReport;

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

        reviewReport = ReviewReport.builder()
                .review(review)
                .reporter(reporter)
                .reason(ReportReason.ABUSIVE_LANGUAGE)
                .description("욕설이 포함되어 있습니다.")
                .build();
        ReflectionTestUtils.setField(reviewReport, "reportId", 1L);
        ReflectionTestUtils.setField(reviewReport, "createdAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("리뷰 신고 저장 성공")
    void save_Success() {
        // given
        given(reviewReportJpaRepository.save(reviewReport)).willReturn(reviewReport);

        // when
        ReviewReport savedReport = reviewReportRepository.save(reviewReport);

        // then
        assertThat(savedReport).isEqualTo(reviewReport);
        verify(reviewReportJpaRepository).save(reviewReport);
    }

    @Test
    @DisplayName("리뷰와 신고자로 신고 조회 성공")
    void findByReviewAndReporter_Success() {
        // given
        given(reviewReportJpaRepository.findByReviewAndReporter(review, reporter))
                .willReturn(Optional.of(reviewReport));

        // when
        Optional<ReviewReport> result = reviewReportRepository.findByReviewAndReporter(review, reporter);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(reviewReport);
        verify(reviewReportJpaRepository).findByReviewAndReporter(review, reporter);
    }

    @Test
    @DisplayName("리뷰와 신고자로 신고 조회 - 결과 없음")
    void findByReviewAndReporter_NotFound() {
        // given
        given(reviewReportJpaRepository.findByReviewAndReporter(review, reporter))
                .willReturn(Optional.empty());

        // when
        Optional<ReviewReport> result = reviewReportRepository.findByReviewAndReporter(review, reporter);

        // then
        assertThat(result).isNotPresent();
        verify(reviewReportJpaRepository).findByReviewAndReporter(review, reporter);
    }

    @Test
    @DisplayName("중복 신고 존재 여부 확인 - 존재함")
    void existsByReviewAndReporter_Exists() {
        // given
        given(reviewReportJpaRepository.existsByReviewAndReporter(review, reporter))
                .willReturn(true);

        // when
        boolean exists = reviewReportRepository.existsByReviewAndReporter(review, reporter);

        // then
        assertThat(exists).isTrue();
        verify(reviewReportJpaRepository).existsByReviewAndReporter(review, reporter);
    }

    @Test
    @DisplayName("중복 신고 존재 여부 확인 - 존재하지 않음")
    void existsByReviewAndReporter_NotExists() {
        // given
        given(reviewReportJpaRepository.existsByReviewAndReporter(review, reporter))
                .willReturn(false);

        // when
        boolean exists = reviewReportRepository.existsByReviewAndReporter(review, reporter);

        // then
        assertThat(exists).isFalse();
        verify(reviewReportJpaRepository).existsByReviewAndReporter(review, reporter);
    }

    @Test
    @DisplayName("리뷰별 신고 목록 조회 성공")
    void findByReview_Success() {
        // given
        List<ReviewReport> reports = List.of(reviewReport);
        given(reviewReportJpaRepository.findByReview(review)).willReturn(reports);

        // when
        List<ReviewReport> result = reviewReportRepository.findByReview(review);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(reviewReport);
        verify(reviewReportJpaRepository).findByReview(review);
    }

    @Test
    @DisplayName("미처리된 신고 목록 조회 성공")
    void findUnprocessedReports_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<ReviewReport> reports = List.of(reviewReport);
        Page<ReviewReport> reportPage = new PageImpl<>(reports, pageable, 1);
        
        given(reviewReportJpaRepository.findByIsProcessedFalseOrderByCreatedAtDesc(pageable))
                .willReturn(reportPage);

        // when
        Page<ReviewReport> result = reviewReportRepository.findUnprocessedReports(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(reviewReport);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        verify(reviewReportJpaRepository).findByIsProcessedFalseOrderByCreatedAtDesc(pageable);
    }

    @Test
    @DisplayName("리뷰별 신고 개수 조회 성공")
    void countByReview_Success() {
        // given
        long expectedCount = 3L;
        given(reviewReportJpaRepository.countByReview(review)).willReturn(expectedCount);

        // when
        long count = reviewReportRepository.countByReview(review);

        // then
        assertThat(count).isEqualTo(expectedCount);
        verify(reviewReportJpaRepository).countByReview(review);
    }

    @Test
    @DisplayName("신고 삭제 성공")
    void deleteById_Success() {
        // given
        Long reportId = 1L;

        // when
        reviewReportRepository.deleteById(reportId);

        // then
        verify(reviewReportJpaRepository).deleteById(reportId);
    }

    @Test
    @DisplayName("빈 리스트 반환 시 처리")
    void findByReview_EmptyList() {
        // given
        given(reviewReportJpaRepository.findByReview(review)).willReturn(List.of());

        // when
        List<ReviewReport> result = reviewReportRepository.findByReview(review);

        // then
        assertThat(result).isEmpty();
        verify(reviewReportJpaRepository).findByReview(review);
    }

    @Test
    @DisplayName("신고 개수가 0일 때 처리")
    void countByReview_ZeroCount() {
        // given
        given(reviewReportJpaRepository.countByReview(review)).willReturn(0L);

        // when
        long count = reviewReportRepository.countByReview(review);

        // then
        assertThat(count).isEqualTo(0L);
        verify(reviewReportJpaRepository).countByReview(review);
    }
}