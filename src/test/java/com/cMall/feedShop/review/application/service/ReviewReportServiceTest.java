package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.review.application.dto.request.ReviewReportRequest;
import com.cMall.feedShop.review.application.dto.response.ReportedReviewResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewReportResponse;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewReport;
import com.cMall.feedShop.review.domain.enums.ReportReason;
import com.cMall.feedShop.review.domain.enums.ReviewStatus;
import com.cMall.feedShop.review.domain.exception.DuplicateReportException;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.review.domain.repository.ReviewReportRepository;
import com.cMall.feedShop.user.domain.exception.UserNotFoundException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.product.domain.model.Category;
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
@DisplayName("ReviewReportService 테스트")
class ReviewReportServiceTest {

    @InjectMocks
    private ReviewReportService reviewReportService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewReportRepository reviewReportRepository;

    @Mock
    private UserRepository userRepository;

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

        UserProfile reviewerProfile = UserProfile.builder()
                .nickname("리뷰어")
                .build();
        ReflectionTestUtils.setField(reviewer, "userProfile", reviewerProfile);

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

        UserProfile reporterProfile = UserProfile.builder()
                .nickname("신고자")
                .build();
        ReflectionTestUtils.setField(reporter, "userProfile", reporterProfile);

        reviewReport = ReviewReport.builder()
                .review(review)
                .reporter(reporter)
                .reason(ReportReason.ABUSIVE_LANGUAGE)
                .description("욕설이 포함되어 있습니다.")
                .build();
        ReflectionTestUtils.setField(reviewReport, "reportId", 1L);
        ReflectionTestUtils.setField(reviewReport, "createdAt", LocalDateTime.now());

        ReflectionTestUtils.setField(reviewReportService, "autoHideThreshold", 5);
    }

    @Test
    @DisplayName("리뷰 신고 성공")
    void reportReview_Success() {
        // given
        Long reviewId = 1L;
        String reporterLoginId = "reporter";
        ReviewReportRequest request = new ReviewReportRequest(ReportReason.ABUSIVE_LANGUAGE, "욕설이 포함되어 있습니다.");

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findByLoginId(reporterLoginId)).willReturn(Optional.of(reporter));
        given(reviewReportRepository.existsByReviewAndReporter(review, reporter)).willReturn(false);
        given(reviewReportRepository.save(any(ReviewReport.class))).willReturn(reviewReport);
        given(reviewReportRepository.countByReview(review)).willReturn(1L);

        // when
        ReviewReportResponse response = reviewReportService.reportReview(reviewId, reporterLoginId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getReportId()).isEqualTo(1L);
        assertThat(response.getReviewId()).isEqualTo(reviewId);
        assertThat(response.getReporterId()).isEqualTo(2L);
        assertThat(response.getReason()).isEqualTo(ReportReason.ABUSIVE_LANGUAGE);
        assertThat(response.getDescription()).isEqualTo("욕설이 포함되어 있습니다.");
        assertThat(response.getIsProcessed()).isFalse();

        verify(reviewRepository).save(review);
        verify(reviewReportRepository).save(any(ReviewReport.class));
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 신고 시 예외 발생")
    void reportReview_ReviewNotFound_ThrowsException() {
        // given
        Long reviewId = 999L;
        String reporterLoginId = "reporter";
        ReviewReportRequest request = new ReviewReportRequest(ReportReason.ABUSIVE_LANGUAGE, "욕설이 포함되어 있습니다.");

        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewReportService.reportReview(reviewId, reporterLoginId, request))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessage("신고할 리뷰를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 신고 시 예외 발생")
    void reportReview_UserNotFound_ThrowsException() {
        // given
        Long reviewId = 1L;
        String reporterLoginId = "nonexistent";
        ReviewReportRequest request = new ReviewReportRequest(ReportReason.ABUSIVE_LANGUAGE, "욕설이 포함되어 있습니다.");

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findByLoginId(reporterLoginId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewReportService.reportReview(reviewId, reporterLoginId, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("신고자 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("중복 신고 시 예외 발생")
    void reportReview_DuplicateReport_ThrowsException() {
        // given
        Long reviewId = 1L;
        String reporterLoginId = "reporter";
        ReviewReportRequest request = new ReviewReportRequest(ReportReason.ABUSIVE_LANGUAGE, "욕설이 포함되어 있습니다.");

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findByLoginId(reporterLoginId)).willReturn(Optional.of(reporter));
        given(reviewReportRepository.existsByReviewAndReporter(review, reporter)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reviewReportService.reportReview(reviewId, reporterLoginId, request))
                .isInstanceOf(DuplicateReportException.class)
                .hasMessage("이미 신고한 리뷰입니다.");
    }

    @Test
    @DisplayName("신고 임계값 도달 시 리뷰 자동 숨김 처리")
    void reportReview_AutoHideWhenThresholdReached() {
        // given
        Long reviewId = 1L;
        String reporterLoginId = "reporter";
        ReviewReportRequest request = new ReviewReportRequest(ReportReason.ABUSIVE_LANGUAGE, "욕설이 포함되어 있습니다.");

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findByLoginId(reporterLoginId)).willReturn(Optional.of(reporter));
        given(reviewReportRepository.existsByReviewAndReporter(review, reporter)).willReturn(false);
        given(reviewReportRepository.save(any(ReviewReport.class))).willReturn(reviewReport);
        given(reviewReportRepository.countByReview(review)).willReturn(5L);

        // when
        reviewReportService.reportReview(reviewId, reporterLoginId, request);

        // then
        verify(reviewRepository, times(2)).save(review);
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.HIDDEN);
    }

    @Test
    @DisplayName("신고된 리뷰 목록 조회 성공")
    void getReportedReviews_Success() {
        // given
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        List<Long> reviewIds = List.of(1L);
        Page<Long> reviewIdPage = new PageImpl<>(reviewIds, pageable, 1);
        
        List<ReviewReport> reports = List.of(reviewReport);

        given(reviewReportRepository.findDistinctReviewIdsWithUnprocessedReports(pageable)).willReturn(reviewIdPage);
        given(reviewReportRepository.findUnprocessedReportsByReviewIds(reviewIds)).willReturn(reports);

        // when
        PaginatedResponse<ReportedReviewResponse> response = reviewReportService.getReportedReviews(page, size);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getPage()).isEqualTo(page);
        assertThat(response.getSize()).isEqualTo(size);
        assertThat(response.getTotalElements()).isEqualTo(1L);

        ReportedReviewResponse reportedReview = response.getContent().get(0);
        assertThat(reportedReview.getReviewId()).isEqualTo(1L);
        assertThat(reportedReview.getTitle()).isEqualTo("Test Review");
        assertThat(reportedReview.getTotalReportCount()).isEqualTo(1L);
        assertThat(reportedReview.getReports()).hasSize(1);
    }

    @Test
    @DisplayName("리뷰 숨김 처리 성공")
    void hideReview_Success() {
        // given
        Long reviewId = 1L;
        String adminLoginId = "admin";
        List<ReviewReport> reports = List.of(reviewReport);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(reviewReportRepository.findByReview(review)).willReturn(reports);

        // when
        reviewReportService.hideReview(reviewId, adminLoginId);

        // then
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.HIDDEN);
        assertThat(reviewReport.isProcessed()).isTrue();
        verify(reviewRepository).save(review);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 숨김 처리 시 예외 발생")
    void hideReview_ReviewNotFound_ThrowsException() {
        // given
        Long reviewId = 999L;
        String adminLoginId = "admin";

        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewReportService.hideReview(reviewId, adminLoginId))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessage("숨김 처리할 리뷰를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("리뷰 숨김 해제 성공")
    void showReview_Success() {
        // given
        Long reviewId = 1L;
        String adminLoginId = "admin";
        ReflectionTestUtils.setField(review, "status", ReviewStatus.HIDDEN);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // when
        reviewReportService.showReview(reviewId, adminLoginId);

        // then
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.ACTIVE);
        verify(reviewRepository).save(review);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 숨김 해제 시 예외 발생")
    void showReview_ReviewNotFound_ThrowsException() {
        // given
        Long reviewId = 999L;
        String adminLoginId = "admin";

        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewReportService.showReview(reviewId, adminLoginId))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessage("숨김 해제할 리뷰를 찾을 수 없습니다.");
    }
}