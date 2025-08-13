package com.cMall.feedShop.review.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 리뷰 전체 삭제 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDeleteResponse {
    
    private Long deletedReviewId;
    private boolean imagesDeleted;
    private int deletedImageCount;
    private String message;
    
    /**
     * 리뷰 삭제 성공 응답 생성
     */
    public static ReviewDeleteResponse of(Long reviewId, boolean imagesDeleted, int imageCount) {
        return ReviewDeleteResponse.builder()
                .deletedReviewId(reviewId)
                .imagesDeleted(imagesDeleted)
                .deletedImageCount(imageCount)
                .message("리뷰가 성공적으로 삭제되었습니다.")
                .build();
    }
    
    /**
     * 이미지 없는 리뷰 삭제 응답 생성
     */
    public static ReviewDeleteResponse ofNoImages(Long reviewId) {
        return ReviewDeleteResponse.builder()
                .deletedReviewId(reviewId)
                .imagesDeleted(false)
                .deletedImageCount(0)
                .message("리뷰가 성공적으로 삭제되었습니다.")
                .build();
    }
}