package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.review.application.dto.response.ReviewDeleteResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewImageDeleteResponse;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewImage;
import com.cMall.feedShop.review.domain.exception.ReviewAccessDeniedException;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import com.cMall.feedShop.review.domain.repository.ReviewImageRepository;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ReviewDeleteService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewImageService reviewImageService;
    private final ReviewImageRepository reviewImageRepository;

    // 선택적 의존성 주입으로 변경 (GCP만)
    @Autowired(required = false)
    private StorageService gcpStorageService;

    // 수동 생성자 (필수 의존성만)
    public ReviewDeleteService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            ReviewImageService reviewImageService,
            ReviewImageRepository reviewImageRepository) {

        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.reviewImageService = reviewImageService;
        this.reviewImageRepository = reviewImageRepository;
    }

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
     * 이메일로 사용자 조회
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + email));
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

    /**
     * 리뷰 수정 권한 검증 (이미지 삭제 시 사용)
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
}