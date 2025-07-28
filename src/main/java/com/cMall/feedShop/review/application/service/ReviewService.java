package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewListResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewResponse;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    // 리뷰 생성
    public ReviewCreateResponse createReview(ReviewCreateRequest request) {
        log.info("리뷰 생성 요청: 상품ID={}, 평점={}", request.getProductId(), request.getRating());

        User currentUser = getCurrentUser();

        // TODO: SPRINT 2에서 주문 검증 로직 추가
        // validateUserPurchasedProduct(currentUser.getId(), request.getProductId());

        // TODO: SPRINT 2에서 중복 리뷰 검증 로직 추가
        // validateNoDuplicateReview(currentUser.getId(), request.getProductId());

        Review review = Review.builder()
                .title(request.getTitle())
                .rating(request.getRating())
                .sizeFit(request.getSizeFit())
                .cushion(request.getCushion())
                .stability(request.getStability())
                .content(request.getContent())
                .user(currentUser)
                .productId(request.getProductId())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("리뷰 생성 완료: ID={}", savedReview.getReviewId());

        return ReviewCreateResponse.of(savedReview.getReviewId());
    }

    // 상품별 리뷰 목록 조회
    @Transactional(readOnly = true)
    public ReviewListResponse getProductReviews(Long productId, int page, int size, String sort) {
        log.info("상품 리뷰 목록 조회: 상품ID={}, 페이지={}, 크기={}, 정렬={}", productId, page, size, sort);

        // 페이지 검증
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 20;

        Pageable pageable = PageRequest.of(page, size);

        Page<Review> reviewPage;
        if ("points".equals(sort)) {
            reviewPage = reviewRepository.findActiveReviewsByProductIdOrderByPoints(productId, pageable);
        } else {
            reviewPage = reviewRepository.findActiveReviewsByProductId(productId, pageable);
        }

        Page<ReviewResponse> reviewResponsePage = reviewPage.map(ReviewResponse::from);

        // 통계 정보 조회
        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
        Long totalReviews = reviewRepository.countActiveReviewsByProductId(productId);

        log.info("리뷰 목록 조회 완료: 총 {}개, 평균 평점 {}", totalReviews, averageRating);

        return ReviewListResponse.of(reviewResponsePage, averageRating, totalReviews);
    }

    // 리뷰 상세 조회
    @Transactional(readOnly = true)
    public ReviewResponse getReview(Long reviewId) {
        log.info("리뷰 상세 조회: ID={}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("ID " + reviewId + "에 해당하는 리뷰를 찾을 수 없습니다."));

        if (!review.isActive()) {
            throw new ReviewNotFoundException("삭제되었거나 숨김 처리된 리뷰입니다.");
        }

        return ReviewResponse.from(review);
    }

    // 현재 사용자 조회
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}