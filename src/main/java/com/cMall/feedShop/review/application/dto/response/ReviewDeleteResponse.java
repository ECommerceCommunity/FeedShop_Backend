package com.cMall.feedShop.review.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "리뷰 삭제 성공 응늵")
public class ReviewDeleteResponse {
    
    @Schema(description = "삭제된 리뷰 ID", example = "123")
    private Long deletedReviewId;
    
    @Schema(description = "이미지 삭제 여부", example = "true")
    private boolean imagesDeleted;
    
    @Schema(description = "삭제된 이미지 개수", example = "3")
    private int deletedImageCount;
    
    @Schema(description = "성공 메시지", example = "리뷰가 성공적으로 삭제되었습니다.")
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