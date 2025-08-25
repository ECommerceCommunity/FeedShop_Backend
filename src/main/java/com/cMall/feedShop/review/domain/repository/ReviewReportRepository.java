package com.cMall.feedShop.review.domain.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewReport;
import com.cMall.feedShop.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReviewReportRepository {
    ReviewReport save(ReviewReport reviewReport);
    Optional<ReviewReport> findByReviewAndReporter(Review review, User reporter);
    boolean existsByReviewAndReporter(Review review, User reporter);
    List<ReviewReport> findByReview(Review review);
    Page<ReviewReport> findUnprocessedReports(Pageable pageable);
    long countByReview(Review review);
    void deleteById(Long reportId);
}