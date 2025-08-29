package com.cMall.feedShop.review.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 리뷰 이미지 삭제 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 이미지 삭제 성공 응늵")
public class ReviewImageDeleteResponse {
    
    @Schema(description = "리뷰 ID", example = "123")
    private Long reviewId;
    
    @Schema(description = "삭제된 이미지 ID 목록", example = "[1, 2, 3]")
    private List<Long> deletedImageIds;
    
    @Schema(description = "삭제된 이미지 개수", example = "3")
    private int deletedImageCount;
    
    @Schema(description = "남은 이미지 개수", example = "2")
    private int remainingImageCount;
    
    @Schema(description = "성공 메시지", example = "3개의 이미지가 성공적으로 삭제되었습니다.")
    private String message;
    
    /**
     * 이미지 삭제 성공 응답 생성
     */
    public static ReviewImageDeleteResponse of(Long reviewId, List<Long> deletedImageIds, int remainingCount) {
        return ReviewImageDeleteResponse.builder()
                .reviewId(reviewId)
                .deletedImageIds(deletedImageIds)
                .deletedImageCount(deletedImageIds.size())
                .remainingImageCount(remainingCount)
                .message(deletedImageIds.size() + "개의 이미지가 성공적으로 삭제되었습니다.")
                .build();
    }
    
    /**
     * 단일 이미지 삭제 응답 생성
     */
    public static ReviewImageDeleteResponse ofSingle(Long reviewId, Long deletedImageId, int remainingCount) {
        return ReviewImageDeleteResponse.builder()
                .reviewId(reviewId)
                .deletedImageIds(List.of(deletedImageId))
                .deletedImageCount(1)
                .remainingImageCount(remainingCount)
                .message("이미지가 성공적으로 삭제되었습니다.")
                .build();
    }
    
    /**
     * 모든 이미지 삭제 응답 생성
     */
    public static ReviewImageDeleteResponse ofAll(Long reviewId, List<Long> deletedImageIds) {
        return ReviewImageDeleteResponse.builder()
                .reviewId(reviewId)
                .deletedImageIds(deletedImageIds)
                .deletedImageCount(deletedImageIds.size())
                .remainingImageCount(0)
                .message("리뷰의 모든 이미지가 삭제되었습니다.")
                .build();
    }
}