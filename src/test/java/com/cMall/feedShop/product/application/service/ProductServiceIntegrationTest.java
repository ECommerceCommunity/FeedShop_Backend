package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.request.ProductOptionRequest;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.store.domain.model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 통합 테스트")
class ProductServiceIntegrationTest {

    @Mock
    private ProductImageService productImageService;

    private ProductService productService;
    private Store store;
    private Category category;

    @BeforeEach
    void setUp() {
        productService = new ProductService(
                null, null, null, null, null, null, productImageService
        );

        store = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(store, "storeId", 1L);

        category = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(category, "categoryId", 1L);
    }

    @Test
    @DisplayName("상품 옵션 추가 테스트 - 정상적인 옵션 리스트")
    void addOptions_ValidOptions_Success() {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        ProductOptionRequest optionRequest1 = new ProductOptionRequest();
        ReflectionTestUtils.setField(optionRequest1, "gender", Gender.MEN);
        ReflectionTestUtils.setField(optionRequest1, "size", Size.SIZE_270);
        ReflectionTestUtils.setField(optionRequest1, "color", Color.BLACK);
        ReflectionTestUtils.setField(optionRequest1, "stock", 50);

        ProductOptionRequest optionRequest2 = new ProductOptionRequest();
        ReflectionTestUtils.setField(optionRequest2, "gender", Gender.WOMEN);
        ReflectionTestUtils.setField(optionRequest2, "size", Size.SIZE_240);
        ReflectionTestUtils.setField(optionRequest2, "color", Color.WHITE);
        ReflectionTestUtils.setField(optionRequest2, "stock", 30);

        List<ProductOptionRequest> requests = Arrays.asList(optionRequest1, optionRequest2);

        // when
        productService.addOptions(product, requests);

        // then
        assertThat(product.getProductOptions()).hasSize(2);
        
        assertThat(product.getProductOptions().get(0).getGender()).isEqualTo(Gender.MEN);
        assertThat(product.getProductOptions().get(0).getSize()).isEqualTo(Size.SIZE_270);
        assertThat(product.getProductOptions().get(0).getColor()).isEqualTo(Color.BLACK);
        assertThat(product.getProductOptions().get(0).getStock()).isEqualTo(50);

        assertThat(product.getProductOptions().get(1).getGender()).isEqualTo(Gender.WOMEN);
        assertThat(product.getProductOptions().get(1).getSize()).isEqualTo(Size.SIZE_240);
        assertThat(product.getProductOptions().get(1).getColor()).isEqualTo(Color.WHITE);
        assertThat(product.getProductOptions().get(1).getStock()).isEqualTo(30);
    }

    @Test
    @DisplayName("상품 옵션 추가 테스트 - 빈 리스트")
    void addOptions_EmptyList_NoOptionsAdded() {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        List<ProductOptionRequest> emptyRequests = List.of();

        // when
        productService.addOptions(product, emptyRequests);

        // then
        assertThat(product.getProductOptions()).isEmpty();
    }

    @Test
    @DisplayName("상품 옵션 추가 테스트 - null 리스트")
    void addOptions_NullList_NoOptionsAdded() {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        // when
        productService.addOptions(product, null);

        // then
        assertThat(product.getProductOptions()).isEmpty();
    }

    @Test
    @DisplayName("상품 이미지 업로드 테스트 - MAIN 타입")
    void uploadImages_MainType_Success() {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        List<MultipartFile> mainImages = Arrays.asList(
                new MockMultipartFile("mainImage1", "main1.jpg", "image/jpeg", "main image 1".getBytes()),
                new MockMultipartFile("mainImage2", "main2.jpg", "image/jpeg", "main image 2".getBytes())
        );

        // ProductImageService의 uploadImages 메서드를 Mock으로 설정
        doNothing().when(productImageService).uploadImages(eq(product), eq(mainImages), eq(ImageType.MAIN));

        // when
        productImageService.uploadImages(product, mainImages, ImageType.MAIN);

        // then
        verify(productImageService).uploadImages(product, mainImages, ImageType.MAIN);
    }

    @Test
    @DisplayName("상품 이미지 업로드 테스트 - DETAIL 타입")
    void uploadImages_DetailType_Success() {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        List<MultipartFile> detailImages = Arrays.asList(
                new MockMultipartFile("detailImage", "detail.jpg", "image/jpeg", "detail image".getBytes())
        );

        // ProductImageService의 uploadImages 메서드를 Mock으로 설정
        doNothing().when(productImageService).uploadImages(eq(product), eq(detailImages), eq(ImageType.DETAIL));

        // when
        productImageService.uploadImages(product, detailImages, ImageType.DETAIL);

        // then
        verify(productImageService).uploadImages(product, detailImages, ImageType.DETAIL);
    }

    @Test
    @DisplayName("상품 이미지 업로드 테스트 - 빈 리스트")
    void uploadImages_EmptyList_NoUpload() {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        List<MultipartFile> emptyImages = List.of();

        // ProductImageService의 uploadImages 메서드를 Mock으로 설정
        doNothing().when(productImageService).uploadImages(eq(product), eq(emptyImages), eq(ImageType.MAIN));

        // when
        productImageService.uploadImages(product, emptyImages, ImageType.MAIN);

        // then
        verify(productImageService).uploadImages(product, emptyImages, ImageType.MAIN);
    }

    @Test
    @DisplayName("상품 이미지 업로드 테스트 - null 리스트")
    void uploadImages_NullList_NoUpload() {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        // ProductImageService의 uploadImages 메서드를 Mock으로 설정
        doNothing().when(productImageService).uploadImages(eq(product), eq(null), eq(ImageType.MAIN));

        // when
        productImageService.uploadImages(product, null, ImageType.MAIN);

        // then
        verify(productImageService).uploadImages(product, null, ImageType.MAIN);
    }

    @Test
    @DisplayName("상품 이미지 교체 테스트")
    void replaceImages_Success() {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        // 기존 이미지 추가
        ProductImage existingImage = new ProductImage("old_image.jpg", ImageType.MAIN, product);
        product.getProductImages().add(existingImage);

        List<MultipartFile> newImages = Arrays.asList(
                new MockMultipartFile("newImage", "new_image.jpg", "image/jpeg", "new image".getBytes())
        );

        // ProductImageService의 replaceImages 메서드를 Mock으로 설정
        doNothing().when(productImageService).replaceImages(eq(product), eq(newImages), eq(ImageType.MAIN));

        // when
        productImageService.replaceImages(product, newImages, ImageType.MAIN);

        // then
        verify(productImageService).replaceImages(product, newImages, ImageType.MAIN);
    }

    @Test
    @DisplayName("여러 타입의 이미지 업로드 테스트")
    void uploadImages_MultipleTypes_Success() {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        List<MultipartFile> mainImages = Arrays.asList(
                new MockMultipartFile("mainImage", "main.jpg", "image/jpeg", "main image".getBytes())
        );

        List<MultipartFile> detailImages = Arrays.asList(
                new MockMultipartFile("detailImage", "detail.jpg", "image/jpeg", "detail image".getBytes())
        );

        // ProductImageService Mock 설정
        doNothing().when(productImageService).uploadImages(eq(product), eq(mainImages), eq(ImageType.MAIN));
        doNothing().when(productImageService).uploadImages(eq(product), eq(detailImages), eq(ImageType.DETAIL));

        // when
        productImageService.uploadImages(product, mainImages, ImageType.MAIN);
        productImageService.uploadImages(product, detailImages, ImageType.DETAIL);

        // then
        verify(productImageService).uploadImages(product, mainImages, ImageType.MAIN);
        verify(productImageService).uploadImages(product, detailImages, ImageType.DETAIL);
    }

    @Test
    @DisplayName("상품 옵션과 이미지를 함께 추가하는 통합 테스트")
    void addOptionsAndImages_Integration_Success() {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        // 옵션 요청 생성
        ProductOptionRequest optionRequest = new ProductOptionRequest();
        ReflectionTestUtils.setField(optionRequest, "gender", Gender.UNISEX);
        ReflectionTestUtils.setField(optionRequest, "size", Size.SIZE_260);
        ReflectionTestUtils.setField(optionRequest, "color", Color.RED);
        ReflectionTestUtils.setField(optionRequest, "stock", 100);

        List<ProductOptionRequest> optionRequests = Arrays.asList(optionRequest);

        // 이미지 파일 생성
        List<MultipartFile> images = Arrays.asList(
                new MockMultipartFile("image", "product.jpg", "image/jpeg", "product image".getBytes())
        );

        // Mock 설정
        doNothing().when(productImageService).uploadImages(eq(product), eq(images), eq(ImageType.MAIN));

        // when
        productService.addOptions(product, optionRequests);
        productImageService.uploadImages(product, images, ImageType.MAIN);

        // then
        // 옵션 검증
        assertThat(product.getProductOptions()).hasSize(1);
        assertThat(product.getProductOptions().get(0).getGender()).isEqualTo(Gender.UNISEX);
        assertThat(product.getProductOptions().get(0).getSize()).isEqualTo(Size.SIZE_260);
        assertThat(product.getProductOptions().get(0).getColor()).isEqualTo(Color.RED);
        assertThat(product.getProductOptions().get(0).getStock()).isEqualTo(100);

        // 이미지 업로드 호출 검증
        verify(productImageService).uploadImages(product, images, ImageType.MAIN);
    }

    @Test
    @DisplayName("단일 옵션 추가 테스트")
    void addOptions_SingleOption_Success() {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("75000"))
                .store(store)
                .category(category)
                .build();

        ProductOptionRequest singleOption = new ProductOptionRequest();
        ReflectionTestUtils.setField(singleOption, "gender", Gender.MEN);
        ReflectionTestUtils.setField(singleOption, "size", Size.SIZE_280);
        ReflectionTestUtils.setField(singleOption, "color", Color.BLUE);
        ReflectionTestUtils.setField(singleOption, "stock", 25);

        List<ProductOptionRequest> requests = Arrays.asList(singleOption);

        // when
        productService.addOptions(product, requests);

        // then
        assertThat(product.getProductOptions()).hasSize(1);
        assertThat(product.getProductOptions().get(0).getGender()).isEqualTo(Gender.MEN);
        assertThat(product.getProductOptions().get(0).getSize()).isEqualTo(Size.SIZE_280);
        assertThat(product.getProductOptions().get(0).getColor()).isEqualTo(Color.BLUE);
        assertThat(product.getProductOptions().get(0).getStock()).isEqualTo(25);
    }
}