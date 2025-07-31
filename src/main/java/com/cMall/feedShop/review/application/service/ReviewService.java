package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewListResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewResponse;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.cMall.feedShop.common.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ReviewCreateResponse createReview(ReviewCreateRequest request) {
        // SecurityContext에서 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String userEmail = authentication.getName();

        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Product 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다: " + request.getProductId()));


        Review review = Review.builder()
                .title(request.getTitle())
                .rating(request.getRating())
                .sizeFit(request.getSizeFit())
                .cushion(request.getCushion())
                .stability(request.getStability())
                .content(request.getContent())
                .user(user)
                .product(product)  // 수정된 부분
                .build();

        Review savedReview = reviewRepository.save(review);

        return ReviewCreateResponse.of(savedReview.getReviewId());
    }
    /**
     * 상품별 리뷰 목록 조회
     * @param productId 상품 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 방식
     * @return 리뷰 목록 응답
     */
    @Transactional(readOnly = true)
    public ReviewListResponse getProductReviews(Long productId, int page, int size, String sort) {
        log.info("상품 리뷰 목록 조회: 상품ID={}, 페이지={}, 크기={}, 정렬={}", productId, page, size, sort);

        // 페이지 검증 및 기본값 설정
        page = Math.max(0, page);
        size = (size < 1 || size > 100) ? 20 : size;

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

    /**
     * 리뷰 상세 조회
     * @param reviewId 리뷰 ID
     * @return 리뷰 상세 응답
     */
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

    /**
     * JWT에서 현재 사용자 조회
     * @param userDetails 사용자 인증 정보
     * @return 현재 사용자
     */
    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        String email = userDetails.getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    // TODO: SPRINT 2에서 추가 예정 메서드들
    /*
    private void validateUserPurchasedProduct(Long userId, Long productId) {
        // 사용자가 해당 상품을 구매했는지 검증
        boolean hasPurchased = orderService.hasUserPurchasedProduct(userId, productId);
        if (!hasPurchased) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "구매한 상품에 대해서만 리뷰를 작성할 수 있습니다.");
        }
    }

    private void validateNoDuplicateReview(Long userId, Long productId) {
        // 이미 해당 상품에 대한 리뷰를 작성했는지 검증
        boolean hasReviewed = reviewRepository.existsByUserIdAndProductId(userId, productId);
        if (hasReviewed) {
            throw new DuplicateReviewException();
        }
    }

    @Transactional
    public void updateReview(Long reviewId, ReviewUpdateRequest request, UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다."));

        if (!review.isOwnedBy(currentUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        review.update(request.getTitle(), request.getRating(), request.getContent(),
                     request.getSizeFit(), request.getCushion(), request.getStability());

        reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다."));

        if (!review.isOwnedBy(currentUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        review.delete();
        reviewRepository.save(review);
    }

    @Transactional
    public void addReviewPoint(Long reviewId, UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다."));

        // 자신의 리뷰에는 추천할 수 없음
        if (review.isOwnedBy(currentUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인이 작성한 리뷰에는 추천할 수 없습니다.");
        }

        // TODO: 추천 중복 방지 로직 추가
        review.addPoint();
        reviewRepository.save(review);
    }
    */
}