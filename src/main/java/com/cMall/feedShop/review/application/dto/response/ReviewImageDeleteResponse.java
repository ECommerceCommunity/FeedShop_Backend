package com.cMall.feedShop.review.application.dto.response;

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
public class ReviewImageDeleteResponse {
    
    private Long reviewId;
    private List<Long> deletedImageIds;
    private int deletedImageCount;
    private int remainingImageCount;
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