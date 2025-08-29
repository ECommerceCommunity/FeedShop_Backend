package com.cMall.feedShop.review.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 리뷰 수정 응답 DTO
 *
 * 🔍설명:
 * - 리뷰 수정이 완료된 후 사용자에게 보내는 응답 데이터입니다
 * - 수정된 리뷰 ID와 성공 메시지, 새로 추가된 이미지 URL들을 포함합니다
 * - 클라이언트(프론트엔드)가 수정 결과를 확인할 수 있도록 정보를 제공합니다
 */
@Getter
@Builder
@Schema(description = "리뷰 수정 성공 응늵")
public class ReviewUpdateResponse {

    @Schema(description = "수정된 리뷰 ID", example = "123")
    private Long reviewId;
    
    @Schema(description = "성공 메시지", example = "리뷰가 성공적으로 수정되었습니다.")
    private String message;
    
    @Schema(description = "새로 추가된 이미지 URL 목록", example = "[\"/api/reviews/images/2025/08/28/new_image.jpg\"]")
    private List<String> newImageUrls; // 새로 추가된 이미지들의 URL
    
    @Schema(description = "삭제된 이미지 ID 목록", example = "[1, 2, 3]")
    private List<Long> deletedImageIds; // 삭제된 이미지들의 ID
    
    @Schema(description = "수정 후 총 이미지 개수", example = "3")
    private int totalImageCount; // 수정 후 총 이미지 개수

    /**
     * 기본 성공 응답 생성 (이미지 변경 없는 경우)
     */
    public static ReviewUpdateResponse of(Long reviewId) {
        return ReviewUpdateResponse.builder()
                .reviewId(reviewId)
                .message("리뷰가 성공적으로 수정되었습니다.")
                .newImageUrls(List.of())
                .deletedImageIds(List.of())
                .totalImageCount(0)
                .build();
    }

    /**
     * 이미지 변경 포함 응답 생성
     */
    public static ReviewUpdateResponse of(Long reviewId, List<String> newImageUrls,
                                          List<Long> deletedImageIds, int totalImageCount) {
        return ReviewUpdateResponse.builder()
                .reviewId(reviewId)
                .message("리뷰가 성공적으로 수정되었습니다.")
                .newImageUrls(newImageUrls != null ? newImageUrls : List.of())
                .deletedImageIds(deletedImageIds != null ? deletedImageIds : List.of())
                .totalImageCount(totalImageCount)
                .build();
    }
}