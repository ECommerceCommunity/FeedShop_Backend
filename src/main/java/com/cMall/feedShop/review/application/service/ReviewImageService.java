package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.review.application.dto.response.ReviewImageResponse;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewImage;
import com.cMall.feedShop.review.domain.repository.ReviewImageRepository;
import com.cMall.feedShop.review.infrastructure.config.ReviewImageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

        // 🔥 수정 1: Long을 int로 변환 (34번째 줄)
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
        // 🔥 수정 2: 올바른 Repository 메서드명 사용 (70번째 줄)
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
        // 🔥 수정 3: 이제 Long을 반환하므로 int로 변환
        Long count = reviewImageRepository.countActiveImagesByReviewId(reviewId);
        return count.intValue(); // Long을 int로 안전하게 변환
    }
}