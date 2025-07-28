package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.response.CategoryResponse;
import com.cMall.feedShop.product.domain.enums.CategoryType;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService 테스트")
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private List<Category> categories;

    @BeforeEach
    void setUp() {
        Category sneakers = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(sneakers, "categoryId", 1L);

        Category boots = new Category(CategoryType.BOOTS, "부츠");
        ReflectionTestUtils.setField(boots, "categoryId", 2L);

        Category sandals = new Category(CategoryType.SANDALS, "샌들");
        ReflectionTestUtils.setField(sandals, "categoryId", 3L);

        categories = Arrays.asList(sneakers, boots, sandals);
    }

    @Test
    @DisplayName("모든 카테고리 조회 성공")
    void getAllCategories_Success() {
        // given
        given(categoryRepository.findAll()).willReturn(categories);

        // when
        List<CategoryResponse> response = categoryService.getAllCategories();

        // then
        assertThat(response).hasSize(3);
        assertThat(response.get(0).getName()).isEqualTo("운동화");
        assertThat(response.get(0).getType()).isEqualTo(CategoryType.SNEAKERS);
        assertThat(response.get(1).getName()).isEqualTo("부츠");
        assertThat(response.get(2).getName()).isEqualTo("샌들");

        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("카테고리가 없는 경우 빈 리스트 반환")
    void getAllCategories_EmptyList() {
        // given
        given(categoryRepository.findAll()).willReturn(List.of());

        // when
        List<CategoryResponse> response = categoryService.getAllCategories();

        // then
        assertThat(response).isEmpty();
        verify(categoryRepository, times(1)).findAll();
    }
}