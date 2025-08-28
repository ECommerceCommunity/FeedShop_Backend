package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.common.storage.UploadDirectory;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.repository.ReviewImageRepository;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.review.domain.service.ReviewDuplicationValidator;
import com.cMall.feedShop.review.domain.service.ReviewPurchaseVerificationService;
import com.cMall.feedShop.user.application.service.BadgeService;
import com.cMall.feedShop.user.application.service.PointService;
import com.cMall.feedShop.user.application.service.UserLevelService;
import com.cMall.feedShop.user.domain.model.ActivityType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ReviewCreateService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewDuplicationValidator duplicationValidator;
    private final ReviewPurchaseVerificationService purchaseVerificationService;
    private final ReviewImageService reviewImageService;
    private final ReviewImageRepository reviewImageRepository;
    private final BadgeService badgeService;
    private final UserLevelService userLevelService;
    private final PointService pointService;

    // 선택적 의존성 주입으로 변경 (GCP만)
    @Autowired(required = false)
    private StorageService gcpStorageService;

    // 수동 생성자 (필수 의존성만)
    public ReviewCreateService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            ReviewDuplicationValidator duplicationValidator,
            ReviewPurchaseVerificationService purchaseVerificationService,
            ReviewImageService reviewImageService,
            ReviewImageRepository reviewImageRepository,
            BadgeService badgeService,
            UserLevelService userLevelService,
            PointService pointService) {

        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.duplicationValidator = duplicationValidator;
        this.purchaseVerificationService = purchaseVerificationService;
        this.reviewImageService = reviewImageService;
        this.reviewImageRepository = reviewImageRepository;
        this.badgeService = badgeService;
        this.userLevelService = userLevelService;
        this.pointService = pointService;
    }

    /**
     * 리뷰 생성 (DTO 불변성 적용)
     *
     * @param request 불변 리뷰 생성 요청 DTO
     * @param images 업로드할 이미지 파일들 (별도 파라미터)
     * @return 생성된 리뷰 응답
     */
    @Transactional
    public ReviewCreateResponse createReview(ReviewCreateRequest request, List<MultipartFile> images) {
        // 현재 로그인한 사용자 가져오기
        User user = getCurrentUserFromSecurity();

        // Product 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다: " + request.getProductId()));

        // 중복 리뷰 검증
        duplicationValidator.validateNoDuplicateActiveReview(user.getId(), product.getProductId());
        
        // 구매이력 검증
        purchaseVerificationService.validateUserPurchasedProduct(user, product.getProductId());

        // ✅ DTO에서 직접 값 추출 (불변 필드)
        Review review = Review.builder()
                .title(request.getTitle())
                .rating(request.getRating())
                .sizeFit(request.getSizeFit())
                .cushion(request.getCushion())
                .stability(request.getStability())
                .content(request.getContent())
                .user(user)
                .product(product)
                .build();

        // Review 저장
        Review savedReview = reviewRepository.save(review);

        // GCP Storage만 사용하도록 단순화
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            try {
                log.info("이미지 업로드 시작: {} 개의 파일", images.size());

                // GCP Storage 서비스만 사용
                if (gcpStorageService != null) {
                    log.info("GCP Storage 서비스 사용");
                    List<UploadResult> uploadResults = gcpStorageService.uploadFilesWithDetails(images, UploadDirectory.REVIEWS);

                    if (!uploadResults.isEmpty()) {
                        // UploadResult를 ReviewImage로 저장
                        saveReviewImagesFromUploadResults(savedReview, uploadResults);

                        // URL만 추출해서 응답용으로 사용
                        imageUrls = uploadResults.stream()
                                .map(UploadResult::getFilePath)
                                .collect(Collectors.toList());
                    }
                } else {
                    log.warn("GCP Storage 서비스가 없습니다. 이미지 없이 리뷰만 저장합니다.");
                }

                log.info("이미지 업로드 완료: {}", imageUrls);
            } catch (Exception e) {
                log.error("이미지 업로드 실패했지만 리뷰는 저장됩니다.", e);
                // 이미지 실패해도 리뷰는 정상 저장되도록 예외를 던지지 않음
            }
        }

        // ✅ 로컬 이미지 처리도 별도 파라미터로 처리
        if (images != null && !images.isEmpty()) {
            reviewImageService.saveReviewImages(savedReview, images);
            log.info("리뷰 이미지 업로드 완료 (기존 방식): reviewId={}, imageCount={}",
                    savedReview.getReviewId(), images.size());
        }

        // 리뷰 작성 포인트 적립
        int pointsEarned = awardPointsForReview(user, savedReview.getReviewId());

        // 뱃지 자동 수여 체크
        checkAndAwardBadgesAfterReview(user.getId(), savedReview.getReviewId());

        // 기본 응답 반환 (currentPoints는 Controller에서 별도 처리)
        return ReviewCreateResponse.builder()
                .reviewId(savedReview.getReviewId())
                .message("리뷰가 성공적으로 등록되었습니다.")
                .imageUrls(imageUrls)
                .pointsEarned(pointsEarned)
                .currentPoints(null) // Controller에서 설정될 예정
                .build();
    }

    /**
     * 현재 로그인한 사용자 조회 (Spring Security 기반)
     */
    private User getCurrentUserFromSecurity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        log.info("현재 인증 정보: {}", authentication);
        log.info("인증됨: {}", authentication != null ? authentication.isAuthenticated() : false);
        log.info("인증 타입: {}", authentication != null ? authentication.getClass().getName() : "null");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("인증 정보가 없거나 인증되지 않음: {}", authentication);
            throw new RuntimeException("인증이 필요합니다");
        }
        
        Object principal = authentication.getPrincipal();
        log.info("Principal: {}", principal);
        log.info("Principal 타입: {}", principal.getClass().getName());
        
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
            log.info("UserDetails에서 username 추출: {}", username);
        } else if (principal instanceof User) {
            username = ((User) principal).getEmail();
            log.info("User에서 email 추출: {}", username);
        } else {
            username = principal.toString();
            log.info("toString()에서 username 추출: {}", username);
        }
        
        log.info("사용자 loginId로 조회 시도: {}", username);
        return userRepository.findByLoginId(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * UploadResult를 ReviewImage로 저장하는 헬퍼 메서드
     */
    private void saveReviewImagesFromUploadResults(Review review, List<UploadResult> uploadResults) {
        // TODO: ReviewImage 엔티티 저장 로직 구현
        log.info("ReviewImage 저장: reviewId={}, imageCount={}", review.getReviewId(), uploadResults.size());
    }

    /**
     * 리뷰 작성 포인트 지급
     */
    private int awardPointsForReview(User user, Long reviewId) {
        try {
            pointService.earnPoints(user, 100, "리뷰 작성 보상", reviewId);
            log.info("리뷰 작성 포인트 적립 완료: userId={}, reviewId={}, points={}", 
                    user.getId(), reviewId, 100);
            return 100;
        } catch (Exception e) {
            log.error("리뷰 작성 포인트 적립 실패: userId={}, reviewId={}, error={}", 
                    user.getId(), reviewId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 리뷰 작성 후 뱃지 자동 수여 체크
     */
    private void checkAndAwardBadgesAfterReview(Long userId, Long reviewId) {
        try {
            // 리뷰 관련 뱃지들 체크
            badgeService.checkAndAwardReviewBadges(userId, reviewId);
            
            // 레벨 업 체크
            userLevelService.recordActivity(userId, ActivityType.REVIEW_CREATION, 
                    "리뷰 작성", reviewId, "REVIEW");
            
            log.info("뱃지 및 레벨 체크 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("뱃지 및 레벨 체크 실패: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
}