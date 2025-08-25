package com.cMall.feedShop.review.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewCreateResponse {
    private Long reviewId;
    private String message;
    private List<String> imageUrls;
    private Integer pointsEarned;    // 이번에 적립된 포인트
    private Integer currentPoints;   // 현재 총 보유 포인트

    public static ReviewCreateResponse of(Long reviewId) {
        return ReviewCreateResponse.builder()
                .reviewId(reviewId)
                .message("리뷰가 성공적으로 작성되었습니다.")
                .build();
    }

    /**
     * currentPoints를 업데이트한 새로운 객체 반환
     */
    public ReviewCreateResponse withCurrentPoints(Integer currentPoints) {
        try {
            return ReviewCreateResponse.builder()
                    .reviewId(this.reviewId)
                    .message(this.message != null ? this.message : "리뷰가 성공적으로 작성되었습니다.")
                    .imageUrls(this.imageUrls != null ? this.imageUrls : java.util.Collections.emptyList())
                    .pointsEarned(this.pointsEarned != null ? this.pointsEarned : 0)
                    .currentPoints(currentPoints)
                    .build();
        } catch (Exception e) {
            // 빌더 패턴에서 예외 발생시 안전한 객체 반환
            return ReviewCreateResponse.builder()
                    .reviewId(this.reviewId)
                    .message("리뷰가 성공적으로 작성되었습니다.")
                    .imageUrls(java.util.Collections.emptyList())
                    .pointsEarned(0)
                    .currentPoints(null)
                    .build();
        }
    }

}