package com.cMall.feedShop.review.application.dto.response;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReviewResponse {
    private Long reviewId;
    private String title;
    private Integer rating;
    private SizeFit sizeFit;
    private Cushion cushion;
    private Stability stability;
    private String content;
    private Integer points;
    private Long userId;
    private String userName;
    private Long productId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReviewImageResponse> images;
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