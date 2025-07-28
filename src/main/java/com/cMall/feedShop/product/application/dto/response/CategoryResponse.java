package com.cMall.feedShop.product.application.dto.response;

import com.cMall.feedShop.product.domain.enums.CategoryType;
import com.cMall.feedShop.product.domain.model.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private Long categoryId;
    private CategoryType type;
    private String name;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .type(category.getType())
                .name(category.getName())
                .build();
    }
}
