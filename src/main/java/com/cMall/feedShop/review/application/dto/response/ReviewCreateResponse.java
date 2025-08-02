package com.cMall.feedShop.review.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewCreateResponse {
    private Long reviewId;
    private String message;

    public static ReviewCreateResponse of(Long reviewId) {
        return ReviewCreateResponse.builder()
                .reviewId(reviewId)
                .message("리뷰가 성공적으로 작성되었습니다.")
                .build();
    }

}