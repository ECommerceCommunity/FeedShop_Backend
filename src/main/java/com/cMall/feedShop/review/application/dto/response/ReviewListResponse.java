package com.cMall.feedShop.review.application.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class ReviewListResponse {
    private List<ReviewResponse> reviews;
    private long totalElements;
    private int totalPages;
    private int size;
    private int number;
    private Double averageRating;
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
