package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.calculator.DiscountCalculator;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.store.domain.model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Product 도메인 테스트")
class ProductTest {

    private Product product;
    private Store store;
    private Category category;
    private DiscountCalculator discountCalculator;

    @BeforeEach
    void setUp() {
        store = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(store, "storeId", 1L);

        category = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(category, "categoryId", 1L);

        product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .discountType(DiscountType.RATE_DISCOUNT)
                .discountValue(new BigDecimal("10"))
                .description("테스트 상품입니다.")
                .build();
        ReflectionTestUtils.setField(product, "productId", 1L);

        discountCalculator = new DiscountCalculator();
    }

    @Test
    @DisplayName("Product 생성 성공")
    void createProduct_Success() {
        // when & then
        assertThat(product.getProductId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("테스트 상품");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("50000"));
        assertThat(product.getStore()).isEqualTo(store);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getWishNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("할인가 계산 성공 - 비율 할인")
    void getDiscountPrice_Success_RateDiscount() {
        // when
        BigDecimal discountPrice = product.getDiscountPrice(discountCalculator);

        // then
        assertThat(discountPrice).isEqualTo(new BigDecimal("45000"));
    }

    @Test
    @DisplayName("할인가 계산 성공 - 할인 없음")
    void getDiscountPrice_Success_NoDiscount() {
        // given
        Product noDiscountProduct = Product.builder()
                .name("할인 없는 상품")
                .price(new BigDecimal("30000"))
                .store(store)
                .category(category)
                .discountType(DiscountType.NONE)
                .build();

        // when
        BigDecimal discountPrice = noDiscountProduct.getDiscountPrice(discountCalculator);

        // then
        assertThat(discountPrice).isEqualTo(new BigDecimal("30000"));
    }

    @Test
    @DisplayName("대표 이미지 URL 조회 성공")
    void getMainImageUrl_Success() {
        // given
        List<ProductImage> images = new ArrayList<>();
        images.add(new ProductImage("http://detail1.jpg", ImageType.DETAIL, product));
        images.add(new ProductImage("http://main.jpg", ImageType.MAIN, product));
        images.add(new ProductImage("http://detail2.jpg", ImageType.DETAIL, product));

        ReflectionTestUtils.setField(product, "productImages", images);

        // when
        String mainImageUrl = product.getMainImageUrl();

        // then
        assertThat(mainImageUrl).isEqualTo("http://main.jpg");
    }

    @Test
    @DisplayName("대표 이미지가 없을 때 null 반환")
    void getMainImageUrl_ReturnsNull_WhenNoMainImage() {
        // given
        List<ProductImage> images = new ArrayList<>();
        images.add(new ProductImage("http://detail1.jpg", ImageType.DETAIL, product));
        images.add(new ProductImage("http://detail2.jpg", ImageType.DETAIL, product));

        ReflectionTestUtils.setField(product, "productImages", images);

        // when
        String mainImageUrl = product.getMainImageUrl();

        // then
        assertThat(mainImageUrl).isNull();
    }

    @Test
    @DisplayName("상품 정보 업데이트 성공")
    void updateInfo_Success() {
        // when
        product.updateInfo("업데이트된 상품명", new BigDecimal("60000"), "업데이트된 설명");

        // then
        assertThat(product.getName()).isEqualTo("업데이트된 상품명");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("60000"));
        assertThat(product.getDescription()).isEqualTo("업데이트된 설명");
    }

    @Test
    @DisplayName("상품 정보 부분 업데이트 - 이름만")
    void updateInfo_Partial_NameOnly() {
        // when
        product.updateInfo("새로운 이름", null, null);

        // then
        assertThat(product.getName()).isEqualTo("새로운 이름");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("50000")); // 기존값 유지
        assertThat(product.getDescription()).isEqualTo("테스트 상품입니다."); // 기존값 유지
    }

    @Test
    @DisplayName("할인 정보 업데이트 성공")
    void updateDiscount_Success() {
        // when
        product.updateDiscount(DiscountType.FIXED_DISCOUNT, new BigDecimal("5000"));

        // then
        assertThat(product.getDiscountType()).isEqualTo(DiscountType.FIXED_DISCOUNT);
        assertThat(product.getDiscountValue()).isEqualTo(new BigDecimal("5000"));
    }

    @Test
    @DisplayName("카테고리 업데이트 성공")
    void updateCategory_Success() {
        // given
        Category newCategory = new Category(CategoryType.BOOTS, "부츠");
        ReflectionTestUtils.setField(newCategory, "categoryId", 2L);

        // when
        product.updateCategory(newCategory);

        // then
        assertThat(product.getCategory()).isEqualTo(newCategory);
        assertThat(product.getCategory().getName()).isEqualTo("부츠");
    }

    @Test
    @DisplayName("판매 가능 상태 확인 - 재고 있음")
    void isAvailableForSale_True_WithStock() {
        // given
        List<ProductOption> options = new ArrayList<>();
        options.add(new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 10, product));
        options.add(new ProductOption(Gender.UNISEX, Size.SIZE_255, Color.BLACK, 0, product));

        ReflectionTestUtils.setField(product, "productOptions", options);

        // when
        boolean isAvailable = product.isAvailableForSale();

        // then
        assertThat(isAvailable).isTrue();
    }

    @Test
    @DisplayName("판매 가능 상태 확인 - 재고 없음")
    void isAvailableForSale_False_NoStock() {
        // given
        List<ProductOption> options = new ArrayList<>();
        options.add(new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 0, product));
        options.add(new ProductOption(Gender.UNISEX, Size.SIZE_255, Color.BLACK, 0, product));

        ReflectionTestUtils.setField(product, "productOptions", options);

        // when
        boolean isAvailable = product.isAvailableForSale();

        // then
        assertThat(isAvailable).isFalse();
    }
}