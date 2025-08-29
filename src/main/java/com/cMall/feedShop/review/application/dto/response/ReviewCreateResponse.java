package com.cMall.feedShop.review.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "리뷰 작성 성공 응늵")
public class ReviewCreateResponse {
    @Schema(description = "생성된 리뷰 ID", example = "123")
    private Long reviewId;
    
    @Schema(description = "성공 메시지", example = "리뷰가 성공적으로 작성되었습니다.")
    private String message;
    
    @Schema(description = "업로드된 이미지 URL 목록", example = "[\"/api/reviews/images/2025/08/28/abc123.jpg\"]")
    private List<String> imageUrls;
    
    @Schema(description = "이번에 적립된 포인트", example = "10")
    private Integer pointsEarned;    // 이번에 적립된 포인트
    
    @Schema(description = "현재 총 보유 포인트", example = "150")
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