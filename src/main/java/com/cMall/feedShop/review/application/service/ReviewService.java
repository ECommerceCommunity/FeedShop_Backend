package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.common.storage.UploadDirectory;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.request.ReviewUpdateRequest;
import com.cMall.feedShop.review.application.dto.response.*;
import com.cMall.feedShop.review.application.dto.response.ReviewDeleteResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewImageDeleteResponse;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import com.cMall.feedShop.review.domain.exception.ReviewAccessDeniedException;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewImage;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.review.domain.repository.ReviewImageRepository;
import com.cMall.feedShop.review.domain.service.ReviewDuplicationValidator;
import com.cMall.feedShop.review.domain.service.ReviewPurchaseVerificationService;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
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
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewDuplicationValidator duplicationValidator;
    private final ReviewPurchaseVerificationService purchaseVerificationService;
    private final ReviewImageService reviewImageService;
    private final ReviewImageRepository reviewImageRepository;

    // 선택적 의존성 주입으로 변경 (GCP만)
    @Autowired(required = false)
    private StorageService gcpStorageService;

    // 수동 생성자 (필수 의존성만)
    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            ReviewDuplicationValidator duplicationValidator,
            ReviewPurchaseVerificationService purchaseVerificationService,
            ReviewImageService reviewImageService,
            ReviewImageRepository reviewImageRepository) {

        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.duplicationValidator = duplicationValidator;
        this.purchaseVerificationService = purchaseVerificationService;
        this.reviewImageService = reviewImageService;
        this.reviewImageRepository = reviewImageRepository;

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

        return ReviewCreateResponse.builder()
                .reviewId(savedReview.getReviewId())
                .message("리뷰가 성공적으로 등록되었습니다.")
                .imageUrls(imageUrls)
                .build();
    }

    // =================== 리뷰 수정 메서드 ===================

    /**
     * 리뷰 수정 (이미지 포함) - DTO 불변성 적용
     *
     * @param reviewId 수정할 리뷰 ID
     * @param request 불변 리뷰 수정 요청 DTO
     * @param newImages 새로 추가할 이미지들 (별도 파라미터)
     * @return 수정 결과 응답
     */
    @Transactional
    public ReviewUpdateResponse updateReview(Long reviewId, ReviewUpdateRequest request,
                                             List<MultipartFile> newImages) {

        log.info("리뷰 수정 시작: reviewId={}", reviewId);

        // 1. 현재 로그인한 사용자 정보 가져오기
        User currentUser = getCurrentUserFromSecurity();

        // 2. 수정할 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("ID " + reviewId + "에 해당하는 리뷰를 찾을 수 없습니다."));

        // 3. 수정 권한 확인
        validateUpdatePermission(review, currentUser.getId());

        // 4. ✅ DTO에서 직접 값 추출하여 리뷰 기본 정보 수정
        review.updateReviewInfo(
                request.getTitle(),
                request.getRating(),
                request.getContent(),
                request.getSizeFit(),
                request.getCushion(),
                request.getStability()
        );

        // 5. 이미지 수정 처리
        List<String> newImageUrls = new ArrayList<>();
        List<Long> deletedImageIds = new ArrayList<>();

        try {
            // ✅ DTO에서 삭제할 이미지 ID 목록 추출
            if (request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty()) {
                deletedImageIds = reviewImageService.deleteSelectedImages(reviewId, request.getDeleteImageIds());
                log.info("이미지 삭제 완료: reviewId={}, 삭제된 개수={}", reviewId, deletedImageIds.size());
            }

            // 새 이미지 추가 처리 (별도 파라미터)
            if (newImages != null && !newImages.isEmpty()) {
                newImageUrls = addNewImages(review, newImages);
                log.info("새 이미지 추가 완료: reviewId={}, 추가된 개수={}", reviewId, newImageUrls.size());
            }

        } catch (Exception e) {
            log.error("이미지 처리 중 오류 발생: reviewId={}, error={}", reviewId, e.getMessage(), e);
            // 이미지 처리 실패 시에도 리뷰 텍스트 수정은 유지하고 경고만 로그
            log.warn("이미지 처리는 실패했지만 리뷰 내용 수정은 완료되었습니다.");
        }
        
        // 6. 리뷰 저장
        Review updatedReview = reviewRepository.save(review);

        // 7. 최종 이미지 개수 확인
        int totalImageCount = reviewImageService.getActiveImageCount(reviewId);

        log.info("리뷰 수정 완료: reviewId={}, 총 이미지 수={}", reviewId, totalImageCount);
        // GitHub CI 빌드 오류 해결을 위한 동기화

        return ReviewUpdateResponse.of(
                updatedReview.getReviewId(),
                newImageUrls,
                deletedImageIds,
                totalImageCount
        );
    }

    /**
     * 현재 로그인한 사용자 정보 가져오기
     */
    private User getCurrentUserFromSecurity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        String userEmail = getUserEmailFromAuthentication(authentication);
        return findUserByEmail(userEmail);
    }

    /**
     * Authentication에서 사용자 이메일 추출
     */
    private String getUserEmailFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return ((User) principal).getEmail();
        } else if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return authentication.getName();
        }
    }

    /**
     * 리뷰 수정 권한 검증
     */
    private void validateUpdatePermission(Review review, Long userId) {
        // 리뷰가 활성 상태인지 확인
        if (!review.isActive()) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND, "삭제되었거나 숨김 처리된 리뷰는 수정할 수 없습니다.");
        }
        // 본인이 작성한 리뷰인지 확인
        if (!review.isOwnedBy(userId)) {
            throw new ReviewAccessDeniedException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        log.debug("리뷰 수정 권한 확인 완료: reviewId={}, userId={}", review.getReviewId(), userId);
    }

    /**
     * 새 이미지들 추가 처리
     */
    private List<String> addNewImages(Review review, List<MultipartFile> newImages) {
        List<String> newImageUrls = new ArrayList<>();

        if (newImages == null || newImages.isEmpty()) {
            return newImageUrls;
        }

        try {
            // GCP Storage 사용
            if (gcpStorageService != null) {
                log.info("GCP Storage로 새 이미지 업로드 시작: reviewId={}, 이미지 수={}",
                        review.getReviewId(), newImages.size());

                List<UploadResult> uploadResults =
                        gcpStorageService.uploadFilesWithDetails(newImages, UploadDirectory.REVIEWS);

                if (!uploadResults.isEmpty()) {
                    // UploadResult를 ReviewImage로 저장
                    saveReviewImagesFromUploadResults(review, uploadResults);

                    // URL 추출
                    newImageUrls = uploadResults.stream()
                            .map(UploadResult::getFilePath)
                            .collect(Collectors.toList());
                }
            } else {
                // GCP Storage가 없을 때 기존 로컬 방식 사용
                log.info("로컬 이미지 처리 시작: reviewId={}, 이미지 수={}",
                        review.getReviewId(), newImages.size());

                List<ReviewImage> savedImages = reviewImageService.saveReviewImages(review, newImages);

                // 로컬 이미지 URL 생성 (기본 URL + 파일 경로)
                newImageUrls = savedImages.stream()
                        .map(image -> "/uploads/images/reviews/" + image.getFilePath())
                        .collect(Collectors.toList());

                log.info("로컬 이미지 업로드 완료: reviewId={}, 저장된 이미지 수={}",
                        review.getReviewId(), savedImages.size());
            }

        } catch (Exception e) {
            log.error("새 이미지 추가 실패: reviewId={}", review.getReviewId(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "이미지 업로드에 실패했습니다: " + e.getMessage());
        }

        return newImageUrls;
    }

    /**
     * 리뷰 수정 (간단 버전 - 이미지 없이)
     */
    @Transactional
    public void updateReviewSimple(Long reviewId, ReviewUpdateRequest request) {
        updateReview(reviewId, request, null);
    }

    /**
     * 리뷰 제목만 수정
     */
    @Transactional
    public void updateReviewTitle(Long reviewId, String newTitle) {
        User currentUser = getCurrentUserFromSecurity();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다."));

        validateUpdatePermission(review, currentUser.getId());

        review.updateTitle(newTitle);
        reviewRepository.save(review);

        log.info("리뷰 제목 수정 완료: reviewId={}, newTitle={}", reviewId, newTitle);
    }

    /**
     * 리뷰 평점만 수정
     */
    @Transactional
    public void updateReviewRating(Long reviewId, Integer newRating) {
        User currentUser = getCurrentUserFromSecurity();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다."));

        validateUpdatePermission(review, currentUser.getId());

        review.updateRating(newRating);
        reviewRepository.save(review);

        log.info("리뷰 평점 수정 완료: reviewId={}, newRating={}", reviewId, newRating);
    }

    /**
     * 리뷰 내용만 수정
     */
    @Transactional
    public void updateReviewContent(Long reviewId, String newContent) {
        User currentUser = getCurrentUserFromSecurity();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다."));

        validateUpdatePermission(review, currentUser.getId());

        review.updateContent(newContent);
        reviewRepository.save(review);

        log.info("리뷰 내용 수정 완료: reviewId={}", reviewId);
    }

    // 업로드 결과를 기존 ReviewImage 엔티티로 저장
    private void saveReviewImagesFromUploadResults(Review review, List<UploadResult> uploadResults) {
        log.info("업로드 결과를 ReviewImage 엔티티로 저장 시작: reviewId={}, resultCount={}",
                review.getReviewId(), uploadResults.size());

        try {
            for (int i = 0; i < uploadResults.size(); i++) {
                UploadResult result = uploadResults.get(i);

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
                    .map(UploadResult::getFilePath)
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
                // GCP Storage만 사용
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

    /**
     * 임시 테스트용 사용자 반환 (개발 환경용)
     */
    private User getTestUser() {
        // DB에서 첫 번째 사용자를 가져와서 테스트용으로 사용
        List<User> allUsers = userRepository.findAll();
        if (allUsers.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "테스트용 사용자가 없습니다. 사용자를 먼저 생성해주세요.");
        }
        User testUser = allUsers.get(0);
        log.info("테스트용 사용자 사용: email={}, id={}", testUser.getEmail(), testUser.getId());
        return testUser;
    }

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
        log.info("상품 리뷰 목록 조회 시작: 상품ID={}, 페이지={}, 크기={}, 정렬={}", productId, page, size, sort);
        
        try {

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
            
        } catch (Exception e) {
            log.error("상품 리뷰 목록 조회 중 오류 발생: 상품ID={}, 에러={}", productId, e.getMessage(), e);
            throw new RuntimeException("리뷰 목록 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 필터링이 적용된 상품별 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public ReviewListResponse getProductReviewsWithFilters(Long productId, int page, int size, String sort, 
                                                          Integer rating, String sizeFit, String cushion, String stability) {
        log.info("필터링된 상품 리뷰 목록 조회 시작: 상품ID={}, 평점={}, 착용감={}, 쿠션감={}, 안정성={}", 
                productId, rating, sizeFit, cushion, stability);
        
        try {
            // 페이지 검증 및 기본값 설정
            page = Math.max(0, page);
            size = (size < 1 || size > 100) ? 20 : size;

            Pageable pageable = PageRequest.of(page, size);

            // 문자열을 enum으로 변환
            SizeFit sizeFitEnum = sizeFit != null ? SizeFit.valueOf(sizeFit.toUpperCase()) : null;
            Cushion cushionEnum = cushion != null ? Cushion.valueOf(cushion.toUpperCase()) : null;
            Stability stabilityEnum = stability != null ? Stability.valueOf(stability.toUpperCase()) : null;

            Page<Review> reviewPage = reviewRepository.findActiveReviewsByProductIdWithFilters(
                    productId, rating, sizeFitEnum, cushionEnum, stabilityEnum, pageable);

            List<ReviewResponse> reviewResponses = convertReviewsToResponses(reviewPage.getContent());
            Page<ReviewResponse> reviewResponsePage = new PageImpl<>(
                    reviewResponses, pageable, reviewPage.getTotalElements());

            // 통계 정보 조회 (전체 리뷰 기준)
            Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
            Long totalReviews = reviewRepository.countActiveReviewsByProductId(productId);

            log.info("필터링된 리뷰 목록 조회 완료: 필터링된 {}개, 전체 {}개", reviewPage.getTotalElements(), totalReviews);

            return ReviewListResponse.of(reviewResponsePage, averageRating, totalReviews);
            
        } catch (IllegalArgumentException e) {
            log.error("잘못된 필터 값: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 필터 값입니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("필터링된 상품 리뷰 목록 조회 중 오류 발생: 상품ID={}, 에러={}", productId, e.getMessage(), e);
            throw new RuntimeException("필터링된 리뷰 목록 조회에 실패했습니다: " + e.getMessage(), e);
        }
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
            log.info("✅ 리뷰 이미지 조회 성공: reviewId={}, 이미지 수={}", review.getReviewId(), images.size());
            
            // 이미지 URL 로깅
            if (!images.isEmpty()) {
                images.forEach(image -> 
                    log.info("🖼️ 최종 이미지 URL: reviewImageId={}, url={}", image.getReviewImageId(), image.getImageUrl())
                );
            }
        } catch (Exception e) {
            log.warn("이미지 조회 실패, 빈 리스트 사용: reviewId={}, 에러={}", review.getReviewId(), e.getMessage());
        }

        // 응답 생성 시도
        try {
            return ReviewResponse.from(review, images);
        } catch (Exception e) {
            log.warn("이미지 포함 응답 생성 실패, 기본 응답 생성: reviewId={}, 에러={}", review.getReviewId(), e.getMessage());
            return ReviewResponse.from(review);
        }
    }

    /**
     * 리뷰 수정 가능 여부 확인
     */
    public boolean canUpdateReview(Long reviewId, Long userId) {
        try {
            Review review = reviewRepository.findById(reviewId).orElse(null);
            if (review == null) {
                return false;
            }
            return review.canBeUpdatedBy(userId);
        } catch (Exception e) {
            log.error("리뷰 수정 가능 여부 확인 실패: reviewId={}, userId={}", reviewId, userId, e);
            return false;

        }
    }

    /**
     * 사용자의 리뷰 목록 조회 (마이페이지용)
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getUserReviews(Long userId, int page, int size) {
        // TODO: SPRINT 3에서 구현 예정
        log.info("사용자 리뷰 목록 조회 요청: userId={}, page={}, size={}", userId, page, size);
        return List.of(); // 임시 반환
    }

    // ============= 리뷰 삭제 관련 메서드들 =============

    /**
     * 리뷰 전체 삭제 (리뷰 + 모든 이미지)
     */
    @Transactional
    public ReviewDeleteResponse deleteReview(Long reviewId) {
        log.info("리뷰 전체 삭제 시작: reviewId={}", reviewId);
        
        // 현재 사용자 정보 가져오기
        User currentUser = getCurrentUserFromSecurity();
        
        // 리뷰 조회 및 검증
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("ID " + reviewId + "에 해당하는 리뷰를 찾을 수 없습니다."));
        
        // 삭제 권한 확인
        validateDeletePermission(review, currentUser.getId());
        
        // 연관된 이미지들 먼저 삭제
        int deletedImageCount = 0;
        boolean imagesDeleted = false;
        
        try {
            List<ReviewImage> reviewImages = reviewImageRepository.findByReviewReviewIdAndDeletedFalse(reviewId);
            
            if (!reviewImages.isEmpty()) {
                // 각 이미지 파일 삭제 (GCP Storage에서)
                for (ReviewImage image : reviewImages) {
                    try {
                        if (gcpStorageService != null) {
                            boolean fileDeleted = gcpStorageService.deleteFile(image.getFilePath());
                            if (fileDeleted) {
                                image.delete(); // 소프트 삭제
                                deletedImageCount++;
                            }
                        } else {
                            image.delete(); // GCP 없어도 DB에서는 삭제 처리
                            deletedImageCount++;
                        }
                    } catch (Exception e) {
                        log.warn("이미지 파일 삭제 실패: imageId={}, filePath={}", 
                                image.getReviewImageId(), image.getFilePath(), e);
                    }
                }
                
                reviewImageRepository.saveAll(reviewImages);
                imagesDeleted = deletedImageCount > 0;
                
                log.info("리뷰 이미지 삭제 완료: reviewId={}, 삭제된 이미지 수={}", reviewId, deletedImageCount);
            }
            
        } catch (Exception e) {
            log.error("리뷰 이미지 삭제 중 오류 발생: reviewId={}", reviewId, e);
            // 이미지 삭제 실패해도 리뷰 삭제는 진행
        }
        
        // 리뷰 소프트 삭제
        review.markAsDeleted();
        reviewRepository.save(review);
        
        log.info("리뷰 전체 삭제 완료: reviewId={}, 삭제된 이미지 수={}", reviewId, deletedImageCount);
        
        return ReviewDeleteResponse.of(reviewId, imagesDeleted, deletedImageCount);
    }
    
    /**
     * 리뷰 이미지 일괄 삭제
     */
    @Transactional
    public ReviewImageDeleteResponse deleteReviewImages(Long reviewId, List<Long> imageIds) {
        log.info("리뷰 이미지 일괄 삭제 시작: reviewId={}, imageIds={}", reviewId, imageIds);
        
        // 현재 사용자 정보 가져오기
        User currentUser = getCurrentUserFromSecurity();
        
        // 리뷰 조회 및 권한 검증
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("ID " + reviewId + "에 해당하는 리뷰를 찾을 수 없습니다."));
        
        validateUpdatePermission(review, currentUser.getId());
        
        List<Long> deletedImageIds = reviewImageService.deleteSelectedImages(reviewId, imageIds);
        int remainingImageCount = reviewImageService.getActiveImageCount(reviewId);
        
        log.info("리뷰 이미지 일괄 삭제 완료: reviewId={}, 삭제된 이미지 수={}, 남은 이미지 수={}", 
                reviewId, deletedImageIds.size(), remainingImageCount);
        
        return ReviewImageDeleteResponse.of(reviewId, deletedImageIds, remainingImageCount);
    }
    
    /**
     * 리뷰 이미지 개별 삭제
     */
    @Transactional
    public ReviewImageDeleteResponse deleteReviewImage(Long reviewId, Long imageId) {
        log.info("리뷰 이미지 개별 삭제 시작: reviewId={}, imageId={}", reviewId, imageId);
        
        // 현재 사용자 정보 가져오기
        User currentUser = getCurrentUserFromSecurity();
        
        // 리뷰 조회 및 권한 검증
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("ID " + reviewId + "에 해당하는 리뷰를 찾을 수 없습니다."));
        
        validateUpdatePermission(review, currentUser.getId());
        
        List<Long> deletedImageIds = reviewImageService.deleteSelectedImages(reviewId, List.of(imageId));
        int remainingImageCount = reviewImageService.getActiveImageCount(reviewId);
        
        if (deletedImageIds.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND, "삭제할 이미지를 찾을 수 없습니다.");
        }
        
        log.info("리뷰 이미지 개별 삭제 완료: reviewId={}, imageId={}, 남은 이미지 수={}", 
                reviewId, imageId, remainingImageCount);
        
        return ReviewImageDeleteResponse.ofSingle(reviewId, imageId, remainingImageCount);
    }
    
    /**
     * 리뷰의 모든 이미지 삭제 (리뷰 텍스트는 유지)
     */
    @Transactional
    public ReviewImageDeleteResponse deleteAllReviewImages(Long reviewId) {
        log.info("리뷰 모든 이미지 삭제 시작: reviewId={}", reviewId);
        
        // 현재 사용자 정보 가져오기
        User currentUser = getCurrentUserFromSecurity();
        
        // 리뷰 조회 및 권한 검증
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("ID " + reviewId + "에 해당하는 리뷰를 찾을 수 없습니다."));
        
        validateUpdatePermission(review, currentUser.getId());
        
        // 모든 활성 이미지 조회
        List<ReviewImage> allImages = reviewImageRepository.findByReviewReviewIdAndDeletedFalse(reviewId);
        List<Long> allImageIds = allImages.stream()
                .map(ReviewImage::getReviewImageId)
                .toList();
        
        if (allImageIds.isEmpty()) {
            log.info("삭제할 이미지가 없습니다: reviewId={}", reviewId);
            return ReviewImageDeleteResponse.ofAll(reviewId, List.of());
        }
        
        List<Long> deletedImageIds = reviewImageService.deleteSelectedImages(reviewId, allImageIds);
        
        log.info("리뷰 모든 이미지 삭제 완료: reviewId={}, 삭제된 이미지 수={}", 
                reviewId, deletedImageIds.size());
        
        return ReviewImageDeleteResponse.ofAll(reviewId, deletedImageIds);
    }
    
    /**
     * 리뷰 삭제 권한 검증
     */
    private void validateDeletePermission(Review review, Long userId) {
        // 리뷰가 활성 상태인지 확인
        if (!review.isActive()) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND, "이미 삭제된 리뷰입니다.");
        }
        
        // 본인이 작성한 리뷰인지 확인
        if (!review.isOwnedBy(userId)) {
            throw new ReviewAccessDeniedException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }
        
        log.debug("리뷰 삭제 권한 확인 완료: reviewId={}, userId={}", review.getReviewId(), userId);
    }

    // ============= Repository 미사용 메서드들을 활용한 통계/관리 기능들 =============

    /**
     * 사용자별 삭제된 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getUserDeletedReviews(Long userId) {
        log.info("사용자 삭제된 리뷰 목록 조회: userId={}", userId);
        
        List<Review> deletedReviews = reviewRepository.findDeletedReviewsByUserId(userId);
        
        List<ReviewResponse> responses = deletedReviews.stream()
                .map(this::createReviewResponseSafely)
                .toList();
        
        log.info("사용자 삭제된 리뷰 조회 완료: userId={}, 개수={}", userId, responses.size());
        
        return responses;
    }

    /**
     * 특정 기간 내 삭제된 리뷰들 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getDeletedReviewsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("기간별 삭제된 리뷰 조회: {} ~ {}", startDate, endDate);
        
        List<Review> deletedReviews = reviewRepository.findDeletedReviewsBetween(startDate, endDate);
        
        List<ReviewResponse> responses = deletedReviews.stream()
                .map(this::createReviewResponseSafely)
                .toList();
        
        log.info("기간별 삭제된 리뷰 조회 완료: 기간={} ~ {}, 개수={}", 
                startDate, endDate, responses.size());
        
        return responses;
    }

    /**
     * 상품별 리뷰 통계 조회 (활성/삭제/전체 개수)
     */
    @Transactional(readOnly = true)
    public ReviewStatsResponse getProductReviewStats(Long productId) {
        log.info("상품 리뷰 통계 조회: productId={}", productId);
        
        Long activeCount = reviewRepository.countActiveReviewsByProductId(productId);
        Long deletedCount = reviewRepository.countDeletedReviewsByProductId(productId);
        Long totalCount = reviewRepository.countAllReviewsByProductId(productId);
        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
        
        if (averageRating == null) {
            averageRating = 0.0;
        }
        
        // 삭제율 계산
        double deletionRate = totalCount > 0 ? (double) deletedCount / totalCount * 100 : 0.0;
        
        ReviewStatsResponse response = ReviewStatsResponse.builder()
                .productId(productId)
                .activeReviewCount(activeCount)
                .deletedReviewCount(deletedCount)
                .totalReviewCount(totalCount)
                .averageRating(averageRating)
                .deletionRate(deletionRate)
                .generatedAt(LocalDateTime.now())
                .build();
        
        log.info("상품 리뷰 통계 조회 완료: productId={}, active={}, deleted={}, total={}, avg={}", 
                productId, activeCount, deletedCount, totalCount, averageRating);
        
        return response;
    }

    /**
     * 사용자별 삭제된 리뷰 개수 조회
     */
    @Transactional(readOnly = true)
    public Long getUserDeletedReviewCount(Long userId) {
        log.info("사용자 삭제된 리뷰 개수 조회: userId={}", userId);
        
        Long deletedCount = reviewRepository.countDeletedReviewsByUserId(userId);
        
        log.info("사용자 삭제된 리뷰 개수 조회 완료: userId={}, 삭제된 개수={}", userId, deletedCount);
        
        return deletedCount;
    }

    /**
     * 최근 30일간 삭제된 리뷰 통계
     */
    @Transactional(readOnly = true)
    public PeriodReviewStatsResponse getRecentDeletedReviewStats() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        
        return getDeletedReviewStatsBetween(startDate, endDate);
    }

    /**
     * 특정 기간 삭제된 리뷰 통계
     */
    @Transactional(readOnly = true)
    public PeriodReviewStatsResponse getDeletedReviewStatsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("기간별 삭제된 리뷰 통계 조회: {} ~ {}", startDate, endDate);
        
        List<Review> deletedReviews = reviewRepository.findDeletedReviewsBetween(startDate, endDate);
        
        long totalDeleted = deletedReviews.size();
        long uniqueUsers = deletedReviews.stream()
                .mapToLong(review -> review.getUser().getId())
                .distinct()
                .count();
        long uniqueProducts = deletedReviews.stream()
                .mapToLong(review -> review.getProduct().getProductId())
                .distinct()
                .count();
        
        double averageRatingOfDeleted = deletedReviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
        
        PeriodReviewStatsResponse response = PeriodReviewStatsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalDeletedCount(totalDeleted)
                .uniqueUserCount(uniqueUsers)
                .uniqueProductCount(uniqueProducts)
                .averageRatingOfDeleted(averageRatingOfDeleted)
                .deletedReviews(deletedReviews.stream()
                        .map(review -> PeriodReviewStatsResponse.DeletedReviewSummary.builder()
                                .reviewId(review.getReviewId())
                                .userId(review.getUser().getId())
                                .productId(review.getProduct().getProductId())
                                .rating(review.getRating())
                                .title(review.getTitle())
                                .deletedAt(review.getUpdatedAt())
                                .build())
                        .toList())
                .build();
        
        log.info("기간별 삭제된 리뷰 통계 조회 완료: 기간={} ~ {}, 총 삭제={}, 사용자={}, 상품={}", 
                startDate, endDate, totalDeleted, uniqueUsers, uniqueProducts);
        
        return response;
    }

    // ============= 필요한 응답 DTO들 (내부 클래스) =============

    /**
     * 상품별 리뷰 통계 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewStatsResponse {
        private Long productId;
        private Long activeReviewCount;
        private Long deletedReviewCount;
        private Long totalReviewCount;
        private Double averageRating;
        private Double deletionRate;
        private LocalDateTime generatedAt;
    }

    /**
     * 기간별 삭제된 리뷰 통계 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodReviewStatsResponse {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Long totalDeletedCount;
        private Long uniqueUserCount;
        private Long uniqueProductCount;
        private Double averageRatingOfDeleted;
        private List<DeletedReviewSummary> deletedReviews;

        /**
         * 삭제된 리뷰 요약 정보
         */
        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DeletedReviewSummary {
            private Long reviewId;
            private Long userId;
            private Long productId;
            private Integer rating;
            private String title;
            private LocalDateTime deletedAt;
        }
    }
}

