package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.response.CategoryResponse;
import com.cMall.feedShop.product.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryReadService {
    private final CategoryRepository categoryRepository;

    // 모든 카테고리 조회
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
