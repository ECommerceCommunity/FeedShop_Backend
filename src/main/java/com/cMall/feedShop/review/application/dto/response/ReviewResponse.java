package com.cMall.feedShop.review.application.dto.response;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "리뷰 상세 정보 응답")
public class ReviewResponse {
    @Schema(description = "리뷰 ID", example = "123")
    private Long reviewId;
    @Schema(description = "리뷰 제목", example = "정말 편한 운동화에요!")
    private String title;
    @Schema(description = "평점 (1-5점)", example = "5")
    private Integer rating;
    @Schema(description = "사이즈 착용감", example = "NORMAL")
    private SizeFit sizeFit;
    @Schema(description = "쿠션감", example = "MEDIUM")
    private Cushion cushion;
    @Schema(description = "안정성", example = "STABLE")
    private Stability stability;
    @Schema(description = "리뷰 내용", example = "3개월 동안 착용해봤는데 정말 편하고 내구성도 좋아요.")
    private String content;
    @Schema(description = "리뷰 점수", example = "15")
    private Integer points;
    @Schema(description = "리뷰 작성자 ID", example = "456")
    private Long userId;
    @Schema(description = "리뷰 작성자 이름", example = "김사용자")
    private String userName;
    
    // 사용자 신체 정보 추가
    @Schema(description = "사용자 키 (cm)", example = "175")
    private Integer userHeight;
    
    @Schema(description = "사용자 몸무게 (kg)", example = "70")
    private Integer userWeight;
    
    @Schema(description = "사용자 발 사이즈 (mm)", example = "260")
    private Integer userFootSize;
    
    @Schema(description = "사용자 발 폭", example = "NORMAL")
    private String userFootWidth;
    
    @Schema(description = "리뷰 대상 상품 ID", example = "1")
    private Long productId;
    
    @Schema(description = "리뷰 작성 일시", example = "2025-08-28T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "리뷰 수정 일시", example = "2025-08-28T15:45:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "리뷰 이미지 목록")
    private List<ReviewImageResponse> images;
    
    @Schema(description = "이미지 보유 여부", example = "true")
    private boolean hasImages;

    public static ReviewResponse from(Review review, List<ReviewImageResponse> imageList) {
        //                                                                      ↑
        //                                                               매개변수명 변경
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .rating(review.getRating())
                .sizeFit(review.getSizeFit())
                .cushion(review.getCushion())
                .stability(review.getStability())
                .content(review.getContent())
                .points(review.getPoints())
                .userId(review.getUser().getId())
                .userName(review.getUser().getUserProfile() != null ?
                        review.getUser().getUserProfile().getName() : "익명")
                // 사용자 신체 정보 추가
                .userHeight(review.getUser().getUserProfile() != null ? 
                        review.getUser().getUserProfile().getHeight() : null)
                .userWeight(review.getUser().getUserProfile() != null ? 
                        review.getUser().getUserProfile().getWeight() : null)
                .userFootSize(review.getUser().getUserProfile() != null ? 
                        review.getUser().getUserProfile().getFootSize() : null)
                .userFootWidth(review.getUser().getUserProfile() != null && 
                        review.getUser().getUserProfile().getFootWidth() != null ?
                        review.getUser().getUserProfile().getFootWidth().name() : null)
                .productId(review.getProduct().getProductId())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .images(imageList)  // ← 매개변수명 사용
                .hasImages(imageList != null && !imageList.isEmpty())
                .build();
    }

    // 기존 메서드도 유지
    public static ReviewResponse from(Review review) {
        return from(review, List.of());
    }
}