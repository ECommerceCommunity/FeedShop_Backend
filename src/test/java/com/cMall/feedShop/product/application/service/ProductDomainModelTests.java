package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.domain.enums.CategoryType;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.store.domain.model.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Product 도메인 모델 테스트")
class ProductDomainModelTests {

    @Test
    @DisplayName("Category 생성 및 getter 테스트")
    void category_Creation_Test() {
        // given & when
        Category category = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(category, "categoryId", 1L);

        // then
        assertThat(category.getCategoryId()).isEqualTo(1L);
        assertThat(category.getType()).isEqualTo(CategoryType.SNEAKERS);
        assertThat(category.getName()).isEqualTo("운동화");
        assertThat(category.getProducts()).isEmpty();
    }

    @Test
    @DisplayName("Store 생성 및 관리자 확인 테스트")
    void store_Creation_And_Manager_Test() {
        // given & when
        Store store = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .description("테스트 설명")
                .logo("http://logo.jpg")
                .build();
        ReflectionTestUtils.setField(store, "storeId", 1L);

        // then
        assertThat(store.getStoreId()).isEqualTo(1L);
        assertThat(store.getStoreName()).isEqualTo("테스트 스토어");
        assertThat(store.getSellerId()).isEqualTo(1L);
        assertThat(store.getDescription()).isEqualTo("테스트 설명");
        assertThat(store.getLogo()).isEqualTo("http://logo.jpg");
        assertThat(store.isManagedBy(1L)).isTrue();
        assertThat(store.isManagedBy(2L)).isFalse();
    }

    @Test
    @DisplayName("Store 관리자 확인 - null 값 처리")
    void store_Manager_Check_Null_Handling() {
        // given
        Store store = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();

        // when & then
        assertThat(store.isManagedBy(null)).isFalse();
        assertThat(store.isManagedBy(1L)).isTrue();
    }

    @Test
    @DisplayName("Category의 모든 CategoryType enum 값 테스트")
    void categoryType_All_Values_Test() {
        // given & when
        CategoryType[] allTypes = CategoryType.values();

        // then
        assertThat(allTypes).hasSize(7);
        assertThat(allTypes).contains(
                CategoryType.SNEAKERS,
                CategoryType.RUNNING,
                CategoryType.BOOTS,
                CategoryType.SANDALS,
                CategoryType.CONVERSE,
                CategoryType.SPORTS,
                CategoryType.DRESS
        );
    }
}