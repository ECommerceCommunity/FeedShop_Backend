package com.cMall.feedShop.product.application.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class ProductPageResponse {
    private List<ProductListResponse> content;
    private long totalElements;
    private int totalPages;
    private int size;
    private int number;

    public static ProductPageResponse of(Page<ProductListResponse> page) {
        return ProductPageResponse.builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .number(page.getNumber())
                .build();
    }
}
