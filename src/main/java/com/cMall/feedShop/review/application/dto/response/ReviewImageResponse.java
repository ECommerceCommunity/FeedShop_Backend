package com.cMall.feedShop.review.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "리뷰 이미지 정보")
public class ReviewImageResponse {
    @Schema(description = "리뷰 이미지 ID", example = "1")
    private Long reviewImageId;
    
    @Schema(description = "원본 파일명", example = "review_image.jpg")
    private String originalFilename;
    
    @Schema(description = "이미지 접근 URL", example = "/api/reviews/images/2025/08/28/abc123.jpg")
    private String imageUrl;
    
    @Schema(description = "이미지 순서", example = "1")
    private Integer imageOrder;
    
    @Schema(description = "파일 크기 (bytes)", example = "1048576")
    private Long fileSize;
}