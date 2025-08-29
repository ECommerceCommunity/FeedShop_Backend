package com.cMall.feedShop.review.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@Schema(description = "리뷰 목록 응늵 (페이지네이션 포함)")
public class ReviewListResponse {
    @Schema(description = "리뷰 목록")
    private List<ReviewResponse> reviews;
    
    @Schema(description = "전체 리뷰 개수", example = "150")
    private long totalElements;
    
    @Schema(description = "전체 페이지 수", example = "8")
    private int totalPages;
    
    @Schema(description = "페이지 크기", example = "20")
    private int size;
    
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private int number;
    
    @Schema(description = "평균 평점", example = "4.2")
    private Double averageRating;
    
    @Schema(description = "전체 리뷰 수", example = "150")
    private Long totalReviews;

    public static ReviewListResponse of(Page<ReviewResponse> reviewPage, Double averageRating, Long totalReviews) {
        return ReviewListResponse.builder()
                .reviews(reviewPage.getContent())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .size(reviewPage.getSize())
                .number(reviewPage.getNumber())
                .averageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .build();
    }
}
