package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewReport;
import com.cMall.feedShop.review.domain.repository.ReviewReportRepository;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewReportRepositoryImpl implements ReviewReportRepository {

    private final ReviewReportJpaRepository reviewReportJpaRepository;

    @Override
    public ReviewReport save(ReviewReport reviewReport) {
        return reviewReportJpaRepository.save(reviewReport);
    }

    @Override
    public Optional<ReviewReport> findByReviewAndReporter(Review review, User reporter) {
        return reviewReportJpaRepository.findByReviewAndReporter(review, reporter);
    }

    @Override
    public boolean existsByReviewAndReporter(Review review, User reporter) {
        return reviewReportJpaRepository.existsByReviewAndReporter(review, reporter);
    }

    @Override
    public List<ReviewReport> findByReview(Review review) {
        return reviewReportJpaRepository.findByReview(review);
    }

    @Override
    public Page<ReviewReport> findUnprocessedReports(Pageable pageable) {
        return reviewReportJpaRepository.findByIsProcessedFalseOrderByCreatedAtDesc(pageable);
    }

    @Override
    public long countByReview(Review review) {
        return reviewReportJpaRepository.countByReview(review);
    }

    @Override
    public void deleteById(Long reportId) {
        reviewReportJpaRepository.deleteById(reportId);
    }

    @Override
    public Page<Long> findDistinctReviewIdsWithUnprocessedReports(Pageable pageable) {
        return reviewReportJpaRepository.findDistinctReviewIdsWithUnprocessedReports(pageable);
    }

    @Override
    public long countDistinctReviewsWithUnprocessedReports() {
        return reviewReportJpaRepository.countDistinctReviewsWithUnprocessedReports();
    }

    @Override
    public List<ReviewReport> findUnprocessedReportsByReviewIds(List<Long> reviewIds) {
        return reviewReportJpaRepository.findUnprocessedReportsByReviewIds(reviewIds);
    }
}