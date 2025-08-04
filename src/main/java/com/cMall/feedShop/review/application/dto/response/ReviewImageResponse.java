package com.cMall.feedShop.review.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewImageResponse {
    private Long reviewImageId;
    private String originalFilename;
    private String imageUrl;
    private Integer imageOrder;
    private Long fileSize;
}