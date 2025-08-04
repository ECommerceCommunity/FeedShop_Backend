package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.service.GcpStorageService;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewImageResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewListResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewResponse;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewImage;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.review.domain.repository.ReviewImageRepository;
import com.cMall.feedShop.review.domain.service.ReviewDuplicationValidator;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service


@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewDuplicationValidator duplicationValidator;
    private final ReviewImageService reviewImageService;
    private final ReviewImageRepository reviewImageRepository;


    // 🔥 수정: 선택적 의존성 주입으로 변경 (GCP만)
    @Autowired(required = false)
    private GcpStorageService gcpStorageService;

    // 🔥 수정: 수동 생성자 (필수 의존성만)
    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            ReviewDuplicationValidator duplicationValidator,
            ReviewImageService reviewImageService,
            ReviewImageRepository reviewImageRepository) {

        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.duplicationValidator = duplicationValidator;
        this.reviewImageService = reviewImageService;
        this.reviewImageRepository = reviewImageRepository;
    }


    @Transactional
    public ReviewCreateResponse createReview(ReviewCreateRequest request, List<MultipartFile> images) {
        // SecurityContext에서 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 디버깅 로그 추가
        log.info("=== 사용자 인증 정보 디버깅 ===");
        log.info("Authentication: {}", authentication);
        log.info("Principal: {}", authentication.getPrincipal());
        log.info("Name: {}", authentication.getName());
        log.info("Authorities: {}", authentication.getAuthorities());


        // Principal에서 직접 이메일 가져오기
        String userEmail;
        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User user = (User) principal;
            userEmail = user.getEmail();
            log.info("Principal에서 직접 이메일 추출: '{}'", userEmail);
        } else if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            userEmail = userDetails.getUsername();
            log.info("UserDetails에서 이메일 추출: '{}'", userEmail);
        } else {
            userEmail = authentication.getName();
            log.info("Authentication.getName()에서 이메일 추출: '{}'", userEmail);
        }

        log.info("Principal 타입: {}", principal.getClass().getSimpleName());
        log.info("Authentication.getName(): '{}'", authentication.getName());
        log.info("최종 조회할 이메일: '{}'", userEmail);

        // 사용자 조회 전 디버깅
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        log.info("사용자 조회 결과: {}", userOptional.isPresent() ? "존재함" : "존재하지 않음");

        if (!userOptional.isPresent()) {
            log.error("데이터베이스에서 이메일 '{}' 로 사용자를 찾을 수 없습니다.", userEmail);

            // 디버깅: 전체 사용자 목록 확인 (개발 환경에서만)
            List<User> allUsers = userRepository.findAll();
            log.info("전체 사용자 수: {}", allUsers.size());
            for (User u : allUsers) {
                log.info("DB에 존재하는 사용자 이메일: '{}'", u.getEmail());
            }

            // 대소문자 무시하고 다시 시도
            log.info("대소문자 무시하고 사용자 재조회 시도...");
            for (User u : allUsers) {
                if (u.getEmail().equalsIgnoreCase(userEmail)) {
                    log.info("대소문자 차이로 인한 문제 발견! DB: '{}', JWT: '{}'", u.getEmail(), userEmail);
                }
            }
        }

        // 사용자 조회 - 여러 방법 시도
        User user = findUserByEmail(userEmail);

        // Product 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다: " + request.getProductId()));

        // 중복 리뷰 검증
        duplicationValidator.validateNoDuplicateActiveReview(user.getId(), product.getProductId());


        // Review 객체를 먼저 생성하고 저장
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


        // 🔥 수정: GCP Storage만 사용하도록 단순화

        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            try {
                log.info("이미지 업로드 시작: {} 개의 파일", images.size());


                // 🔥 GCP Storage 서비스만 사용
                if (gcpStorageService != null) {
                    log.info("GCP Storage 서비스 사용");
                    List<GcpStorageService.UploadResult> uploadResults = gcpStorageService.uploadFilesWithDetails(images, "reviews");

                    if (!uploadResults.isEmpty()) {
                        // UploadResult를 ReviewImage로 저장
                        saveReviewImagesFromUploadResults(savedReview, uploadResults);

                        // URL만 추출해서 응답용으로 사용
                        imageUrls = uploadResults.stream()
                                .map(GcpStorageService.UploadResult::getFilePath)
                                .collect(Collectors.toList());
                    }
                } else {
                    log.warn("GCP Storage 서비스가 없습니다. 이미지 없이 리뷰만 저장합니다.");
                }

                log.info("이미지 업로드 완료: {}", imageUrls);
            } catch (Exception e) {
                log.error("이미지 업로드 실패했지만 리뷰는 저장됩니다.", e);
                // 🔥 이미지 실패해도 리뷰는 정상 저장되도록 예외를 던지지 않음

            }
        }

        // 기존 이미지 처리
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            reviewImageService.saveReviewImages(savedReview, request.getImages());
            log.info("리뷰 이미지 업로드 완료 (기존 방식): reviewId={}, imageCount={}",
                    savedReview.getReviewId(), request.getImages().size());
        }

        return ReviewCreateResponse.builder()
                .reviewId(savedReview.getReviewId())
                .message("리뷰가 성공적으로 등록되었습니다.")
                .imageUrls(imageUrls)
                .build();

    }

    // 업로드 결과를 기존 ReviewImage 엔티티로 저장
    private void saveReviewImagesFromUploadResults(Review review, List<GcpStorageService.UploadResult> uploadResults) {
        log.info("업로드 결과를 ReviewImage 엔티티로 저장 시작: reviewId={}, resultCount={}",
                review.getReviewId(), uploadResults.size());

        try {
            for (int i = 0; i < uploadResults.size(); i++) {
                GcpStorageService.UploadResult result = uploadResults.get(i);

                ReviewImage reviewImage = ReviewImage.builder()
                        .review(review)
                        .originalFilename(result.getOriginalFilename())
                        .storedFilename(result.getStoredFilename())
                        .filePath(result.getFilePath())
                        .fileSize(result.getFileSize())
                        .contentType(result.getContentType())
                        .imageOrder(i)
                        .build();

                ReviewImage savedImage = reviewImageRepository.save(reviewImage);

                log.debug("ReviewImage 저장 완료: id={}, url={}, imageOrder={}",
                        savedImage.getReviewImageId(), result.getFilePath(), i);
            }

            log.info("업로드 결과 저장 완료: reviewId={}, 저장된 이미지 수={}",
                    review.getReviewId(), uploadResults.size());

        } catch (Exception e) {
            log.error("업로드 결과 저장 실패: reviewId={}", review.getReviewId(), e);

            // 이미 업로드된 GCP Storage 파일들 삭제 (롤백)
            List<String> imageUrls = uploadResults.stream()
                    .map(GcpStorageService.UploadResult::getFilePath)
                    .collect(Collectors.toList());
            rollbackUploadedImages(imageUrls);

            throw new RuntimeException("리뷰 이미지 저장에 실패했습니다", e);
        }
    }

    /**
     * 업로드된 이미지들을 GCP Storage에서 삭제 (롤백용)
     */
    private void rollbackUploadedImages(List<String> imageUrls) {
        log.warn("이미지 저장 실패로 인한 GCP Storage 파일 삭제 시작: {} 개의 파일", imageUrls.size());

        for (String imageUrl : imageUrls) {
            try {
                // 🔥 GCP Storage만 사용
                if (gcpStorageService != null) {
                    boolean deleted = gcpStorageService.deleteFile(imageUrl);
                    if (deleted) {
                        log.info("롤백: GCP Storage 파일 삭제 성공: {}", imageUrl);
                    } else {
                        log.warn("롤백: GCP Storage 파일 삭제 실패: {}", imageUrl);
                    }
                } else {
                    log.warn("롤백: GCP Storage 서비스가 없습니다: {}", imageUrl);
                }
            } catch (Exception e) {
                log.error("롤백: 파일 삭제 중 오류: {}", imageUrl, e);
            }
        }
    }

    // 🔥 SPRINT 3에서 구현 예정 - 현재는 주석처리
    /*
    @Transactional
    public void deleteReviewWithImages(Long reviewId) {
        // SPRINT 3에서 구현 예정
        throw new UnsupportedOperationException("리뷰 삭제 기능은 SPRINT 3에서 구현 예정입니다.");
    }
    */

    /**
     * 여러 방법으로 사용자 조회 시도
     */
    private User findUserByEmail(String userEmail) {
        log.info("사용자 조회 시작: email='{}'", userEmail);

        // 1. 기본 조회
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isPresent()) {
            log.info("기본 조회 성공");
            return userOptional.get();
        }

        // 3. 직접 대소문자 무시 조회
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (user.getEmail().equalsIgnoreCase(userEmail)) {
                log.warn("대소문자 차이로 사용자 발견! DB: '{}', 요청: '{}'", user.getEmail(), userEmail);
                return user;
            }
        }

        // 4. 모든 방법 실패
        log.error("모든 방법으로 사용자 조회 실패: email='{}'", userEmail);
        throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail);
    }


    /**
     * 상품별 리뷰 목록 조회
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

        List<ReviewResponse> reviewResponses = convertReviewsToResponses(reviewPage.getContent());
        Page<ReviewResponse> reviewResponsePage = new PageImpl<>(
                reviewResponses, pageable, reviewPage.getTotalElements());

        // 통계 정보 조회
        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
        Long totalReviews = reviewRepository.countActiveReviewsByProductId(productId);

        log.info("리뷰 목록 조회 완료: 총 {}개, 평균 평점 {}", totalReviews, averageRating);

        return ReviewListResponse.of(reviewResponsePage, averageRating, totalReviews);
    }

    /**
     * 리뷰 상세 조회
     */
    @Transactional(readOnly = true)
    public ReviewResponse getReview(Long reviewId) {
        log.info("리뷰 상세 조회: ID={}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("ID " + reviewId + "에 해당하는 리뷰를 찾을 수 없습니다."));

        if (!review.isActive()) {
            throw new ReviewNotFoundException("삭제되었거나 숨김 처리된 리뷰입니다.");
        }

        return createReviewResponseSafely(review);
    }

    /**
     * 리뷰 목록을 응답으로 변환하는 메서드
     */
    private List<ReviewResponse> convertReviewsToResponses(List<Review> reviews) {
        return reviews.stream()
                .map(this::createReviewResponseSafely)
                .toList();
    }

    /**
     * 단일 리뷰를 안전하게 응답으로 변환
     */
    private ReviewResponse createReviewResponseSafely(Review review) {
        List<ReviewImageResponse> images = List.of(); // 기본값

        // 이미지 조회 시도
        try {
            images = reviewImageService.getReviewImages(review.getReviewId());
        } catch (Exception e) {
            log.debug("이미지 조회 실패, 빈 리스트 사용: reviewId={}", review.getReviewId());
        }

        // 응답 생성 시도
        try {
            return ReviewResponse.from(review, images);
        } catch (Exception e) {
            log.debug("이미지 포함 응답 생성 실패, 기본 응답 생성: reviewId={}", review.getReviewId());
            return ReviewResponse.from(review);
        }
    }

    /**
     * JWT에서 현재 사용자 조회
     */
    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        String email = userDetails.getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    // TODO: SPRINT 3에서 추가 예정 메서드들
    /*
    @Transactional
    public void updateReview(Long reviewId, ReviewUpdateRequest request, UserDetails userDetails) {
        // SPRINT 3에서 구현 예정
    }

    @Transactional
    public void deleteReview(Long reviewId, UserDetails userDetails) {
        // SPRINT 3에서 구현 예정
    }

    @Transactional
    public void deleteReviewWithImages(Long reviewId) {
        // SPRINT 3에서 구현 예정
    }
    */

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

