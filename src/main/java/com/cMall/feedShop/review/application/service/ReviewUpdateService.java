package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.common.storage.UploadDirectory;
import com.cMall.feedShop.review.application.dto.request.ReviewUpdateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewUpdateResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ReviewUpdateService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewImageService reviewImageService;
    private final ReviewImageRepository reviewImageRepository;

    // 선택적 의존성 주입으로 변경 (GCP만)
    @Autowired(required = false)
    private StorageService gcpStorageService;

    // 수동 생성자 (필수 의존성만)
    public ReviewUpdateService(
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
     * 업로드 결과를 기존 ReviewImage 엔티티로 저장
     */
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
}