package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewReport;
import com.cMall.feedShop.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewReportJpaRepository extends JpaRepository<ReviewReport, Long> {

    Optional<ReviewReport> findByReviewAndReporter(Review review, User reporter);

    boolean existsByReviewAndReporter(Review review, User reporter);

    List<ReviewReport> findByReview(Review review);

    @Query("SELECT rr FROM ReviewReport rr " +
           "JOIN FETCH rr.review r " +
           "JOIN FETCH rr.reporter u " +
           "WHERE rr.isProcessed = false " +
           "ORDER BY rr.createdAt DESC")
    Page<ReviewReport> findByIsProcessedFalseOrderByCreatedAtDesc(Pageable pageable);

    long countByReview(Review review);

    @Query("SELECT COUNT(rr) FROM ReviewReport rr " +
           "WHERE rr.review = :review AND rr.isProcessed = false")
    long countUnprocessedByReview(@Param("review") Review review);
}