package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.review.application.dto.response.ReviewImageResponse;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewImage;
import com.cMall.feedShop.review.domain.repository.ReviewImageRepository;
import com.cMall.feedShop.review.infrastructure.config.ReviewImageProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewImageService {

    private final ReviewImageRepository reviewImageRepository;
    private final ReviewImageUploadService uploadService;
    private final ReviewImageProperties imageProperties;

    @Transactional
    public List<ReviewImage> saveReviewImages(Review review, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return List.of();
        }

        // Long을 int로 변환
        Long currentImageCountLong = reviewImageRepository.countActiveImagesByReviewId(review.getReviewId());
        int currentImageCount = currentImageCountLong.intValue(); // Long을 int로 안전하게 변환

        uploadService.validateImageCount(currentImageCount, imageFiles.size());

        return IntStream.range(0, imageFiles.size())
                .mapToObj(index -> {
                    MultipartFile file = imageFiles.get(index);
                    ReviewImageUploadService.ReviewImageUploadInfo uploadInfo = uploadService.uploadImage(file);

                    ReviewImage reviewImage = ReviewImage.builder()
                            .originalFilename(uploadInfo.getOriginalFilename())
                            .storedFilename(uploadInfo.getStoredFilename())
                            .filePath(uploadInfo.getFilePath())
                            .fileSize(uploadInfo.getFileSize())
                            .contentType(uploadInfo.getContentType())
                            .imageOrder(currentImageCount + index + 1)
                            .review(review)
                            .build();

                    return reviewImageRepository.save(reviewImage);
                })
                .toList();
    }

    @Transactional
    public void deleteReviewImages(Long reviewId) {
        // 수정된 메서드명 사용
        List<ReviewImage> images = reviewImageRepository.findActiveImagesByReviewId(reviewId);

        for (ReviewImage image : images) {
            image.delete();
            uploadService.deleteImage(image.getFilePath());
        }

        log.info("리뷰 이미지 삭제 완료: reviewId={}, count={}", reviewId, images.size());
    }

    public List<ReviewImageResponse> getReviewImages(Long reviewId) {
        List<ReviewImage> images = reviewImageRepository.findActiveImagesByReviewId(reviewId);

        return images.stream()
                .map(image -> ReviewImageResponse.builder()
                        .reviewImageId(image.getReviewImageId())
                        .originalFilename(image.getOriginalFilename())
                        .imageUrl(image.getFullImageUrl(imageProperties.getBaseUrl()))
                        .imageOrder(image.getImageOrder())
                        .fileSize(image.getFileSize())
                        .build())
                .toList();
    }

    public int getActiveImageCount(Long reviewId) {
        Long count = reviewImageRepository.countActiveImagesByReviewId(reviewId);
        return count.intValue(); // Long을 int로 안전하게 변환
    }

    // =================== 리뷰 수정용 이미지 관리 메서드들 ===================

    /**
     * 리뷰 수정 시 이미지 업데이트 (삭제 + 추가)
     */
    @Transactional
    public List<ReviewImage> updateReviewImages(Review review,
                                                List<Long> deleteImageIds,
                                                List<MultipartFile> newImageFiles) {

        log.info("리뷰 이미지 업데이트 시작: reviewId={}, 삭제할 이미지 수={}, 새 이미지 수={}",
                review.getReviewId(),
                deleteImageIds != null ? deleteImageIds.size() : 0,
                newImageFiles != null ? newImageFiles.size() : 0);

        // 1. 기존 이미지 삭제 처리
        List<Long> deletedImageIds = deleteSelectedImages(review.getReviewId(), deleteImageIds);

        // 2. 현재 남은 이미지 개수 확인
        int remainingImageCount = getActiveImageCount(review.getReviewId());

        // 3. 새 이미지 추가 (개수 제한 검증 포함)
        List<ReviewImage> newImages = List.of();
        if (newImageFiles != null && !newImageFiles.isEmpty()) {
            uploadService.validateImageCount(remainingImageCount, newImageFiles.size());
            newImages = saveReviewImages(review, newImageFiles);
        }

        log.info("리뷰 이미지 업데이트 완료: reviewId={}, 삭제된 이미지={}, 추가된 이미지={}",
                review.getReviewId(), deletedImageIds.size(), newImages.size());

        return newImages;
    }

    /**
     * 선택된 이미지들을 삭제 (성능 최적화 적용)
     *
     * 🚀 성능 최적화:
     * - findActiveImagesByReviewIdAndImageIds 메서드로 필요한 이미지만 DB에서 직접 조회
     * - 전체 이미지를 메모리로 가져온 후 필터링하는 비효율 제거
     */
    @Transactional
    public List<Long> deleteSelectedImages(Long reviewId, List<Long> deleteImageIds) {
        if (deleteImageIds == null || deleteImageIds.isEmpty()) {
            return List.of();
        }

        log.info("선택된 이미지 삭제 시작: reviewId={}, 삭제 대상 이미지 ID={}", reviewId, deleteImageIds);

        // ✨ 성능 최적화: 필요한 이미지만 DB에서 직접 조회
        List<ReviewImage> imagesToDelete = reviewImageRepository
                .findActiveImagesByReviewIdAndImageIds(reviewId, deleteImageIds);

        List<Long> actuallyDeletedIds = new ArrayList<>();

        for (ReviewImage image : imagesToDelete) {
            try {
                // 논리적 삭제
                image.delete();

                // 물리적 파일 삭제 시도
                uploadService.deleteImage(image.getFilePath());

                actuallyDeletedIds.add(image.getReviewImageId());
                log.debug("이미지 삭제 성공: imageId={}, filePath={}",
                        image.getReviewImageId(), image.getFilePath());

            } catch (Exception e) {
                log.error("이미지 삭제 실패: imageId={}, error={}",
                        image.getReviewImageId(), e.getMessage(), e);
                // 개별 이미지 삭제 실패는 무시하고 계속 진행
            }
        }

        log.info("이미지 삭제 완료: reviewId={}, 요청={}, 실제삭제={}",
                reviewId, deleteImageIds.size(), actuallyDeletedIds.size());

        return actuallyDeletedIds;
    }

    /**
     * 리뷰의 특정 이미지만 삭제
     *
     * 🔧 트랜잭션 최적화:
     * - 내부에서 @Transactional이 적용된 deleteSelectedImages를 호출하므로
     * - 불필요한 트랜잭션 중첩을 피하기 위해 @Transactional 어노테이션 제거
     *
     * @param reviewId 리뷰 ID
     * @param imageId 삭제할 이미지 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteSingleImage(Long reviewId, Long imageId) {
        return !deleteSelectedImages(reviewId, List.of(imageId)).isEmpty();
    }

    /**
     * 리뷰 수정 후 이미지 순서 재정렬
     */
    @Transactional
    public void reorderImages(Long reviewId) {
        List<ReviewImage> activeImages = reviewImageRepository.findActiveImagesByReviewId(reviewId);

        // 이미지 순서를 1부터 다시 매기기
        for (int i = 0; i < activeImages.size(); i++) {
            ReviewImage image = activeImages.get(i);
            image.updateOrder(i + 1);
        }

        log.info("이미지 순서 재정렬 완료: reviewId={}, 이미지 수={}", reviewId, activeImages.size());
    }

    /**
     * 리뷰 수정 가능 여부 확인 (이미지 관점)
     */
    public boolean canUpdateImages(Long reviewId, Long userId) {
        // 추가적인 이미지 수정 권한 검증이 필요한 경우 여기에 구현
        // 현재는 기본적으로 허용
        return true;
    }

    /**
     * 리뷰의 총 이미지 개수 제한 확인
     */
    public boolean canAddMoreImages(Long reviewId, int newImageCount) {
        int currentCount = getActiveImageCount(reviewId);
        int totalAfterAdd = currentCount + newImageCount;
        return totalAfterAdd <= imageProperties.getMaxImageCount();
    }

    /**
     * 이미지 업데이트 결과 정보 클래스
     */
    @Getter
    @Builder
    public static class ImageUpdateResult {
        private List<Long> deletedImageIds;
        private List<String> newImageUrls;
        private int totalImageCount;
        private boolean success;
        private String message;

        public static ImageUpdateResult success(List<Long> deletedIds, List<String> newUrls, int totalCount) {
            return ImageUpdateResult.builder()
                    .deletedImageIds(deletedIds)
                    .newImageUrls(newUrls)
                    .totalImageCount(totalCount)
                    .success(true)
                    .message("이미지 업데이트가 완료되었습니다.")
                    .build();
        }

        public static ImageUpdateResult failure(String message) {
            return ImageUpdateResult.builder()
                    .deletedImageIds(List.of())
                    .newImageUrls(List.of())
                    .totalImageCount(0)
                    .success(false)
                    .message(message)
                    .build();
        }
    }
}