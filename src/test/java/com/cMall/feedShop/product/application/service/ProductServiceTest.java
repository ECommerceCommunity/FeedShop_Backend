package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.request.ProductCreateRequest;
import com.cMall.feedShop.product.application.dto.request.ProductOptionRequest;
import com.cMall.feedShop.product.application.dto.request.ProductUpdateRequest;
import com.cMall.feedShop.product.application.dto.response.ProductCreateResponse;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.repository.CategoryRepository;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.store.domain.repository.StoreRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 테스트")
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductImageService productImageService; // 추가
    @Mock private UserDetails userDetails;

    @InjectMocks
    private ProductService productService;

    private User seller;
    private Store store;
    private Category category;
    private ProductCreateRequest createRequest;
    private ProductUpdateRequest updateRequest;
    private Product product;
    private List<MultipartFile> mainImages;
    private List<MultipartFile> detailImages;

    @BeforeEach
    void setUp() {
        setupUser();
        setupStore();
        setupCategory();
        setupCreateRequest();
        setupUpdateRequest();
        setupProduct();
        setupMultipartFiles();
    }

    private void setupUser() {
        seller = new User("sellerId", "password", "seller@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(seller, "id", 1L);
    }

    private void setupStore() {
        store = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(store, "storeId", 1L);
    }

    private void setupCategory() {
        category = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(category, "categoryId", 1L);
    }

    private void setupCreateRequest() {
        createRequest = new ProductCreateRequest();
        ReflectionTestUtils.setField(createRequest, "name", "테스트 상품");
        ReflectionTestUtils.setField(createRequest, "price", new BigDecimal("50000"));
        ReflectionTestUtils.setField(createRequest, "categoryId", 1L);
        ReflectionTestUtils.setField(createRequest, "discountType", DiscountType.RATE_DISCOUNT);
        ReflectionTestUtils.setField(createRequest, "discountValue", new BigDecimal("10"));
        ReflectionTestUtils.setField(createRequest, "description", "테스트 상품 설명");

        ProductOptionRequest optionRequest = new ProductOptionRequest();
        ReflectionTestUtils.setField(optionRequest, "gender", Gender.UNISEX);
        ReflectionTestUtils.setField(optionRequest, "size", Size.SIZE_250);
        ReflectionTestUtils.setField(optionRequest, "color", Color.WHITE);
        ReflectionTestUtils.setField(optionRequest, "stock", 100);

        ReflectionTestUtils.setField(createRequest, "options", Arrays.asList(optionRequest));
    }

    private void setupUpdateRequest() {
        updateRequest = new ProductUpdateRequest();
        ReflectionTestUtils.setField(updateRequest, "name", "업데이트된 상품");
        ReflectionTestUtils.setField(updateRequest, "price", new BigDecimal("60000"));
        ReflectionTestUtils.setField(updateRequest, "categoryId", 1L);
        ReflectionTestUtils.setField(updateRequest, "description", "업데이트된 설명");
    }

    private void setupProduct() {
        product = Product.builder()
                .name("기존 상품")
                .price(new BigDecimal("40000"))
                .store(store)
                .category(category)
                .build();
        ReflectionTestUtils.setField(product, "productId", 1L);
    }

    private void setupMultipartFiles() {
        mainImages = Arrays.asList(
                new MockMultipartFile("mainImage", "main.jpg", "image/jpeg", "main image content".getBytes())
        );
        detailImages = Arrays.asList(
                new MockMultipartFile("detailImage", "detail.jpg", "image/jpeg", "detail image content".getBytes())
        );
    }

    @Test
    @DisplayName("상품 등록 성공")
    void createProduct_Success() {
        // given
        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(storeRepository.findBySellerId(1L)).willReturn(Optional.of(store));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedProduct, "productId", 1L);
            return savedProduct;
        });

        // ProductImageService mock 설정
        List<ProductImage> mockMainImages = Arrays.asList(
                new ProductImage("main.jpg", ImageType.MAIN, null)
        );
        List<ProductImage> mockDetailImages = Arrays.asList(
                new ProductImage("detail.jpg", ImageType.DETAIL, null)
        );
        given(productImageService.uploadImages(any(Product.class), eq(mainImages), eq(ImageType.MAIN)))
                .willReturn(mockMainImages);
        given(productImageService.uploadImages(any(Product.class), eq(detailImages), eq(ImageType.DETAIL)))
                .willReturn(mockDetailImages);

        // when
        ProductCreateResponse response = productService.createProduct(createRequest, mainImages, detailImages, userDetails);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(1L);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productImageService, times(1)).uploadImages(any(Product.class), eq(mainImages), eq(ImageType.MAIN));
        verify(productImageService, times(1)).uploadImages(any(Product.class), eq(detailImages), eq(ImageType.DETAIL));
    }

    @Test
    @DisplayName("상품 등록 성공 - 이미지 없이")
    void createProduct_Success_WithoutImages() {
        // given
        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(storeRepository.findBySellerId(1L)).willReturn(Optional.of(store));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedProduct, "productId", 1L);
            return savedProduct;
        });

        // when
        ProductCreateResponse response = productService.createProduct(createRequest, null, null, userDetails);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(1L);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productImageService, never()).uploadImages(any(), any(), any());
    }

    @Test
    @DisplayName("상품 등록 실패 - 판매자 권한 없음")
    void createProduct_Fail_NotSeller() {
        // given
        User user = new User("userId", "password", "user@test.com", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userDetails.getUsername()).willReturn("user@test.com");
        given(userRepository.findByLoginId("user@test.com")).willReturn(Optional.of(user));

        // when & then
        ProductException thrown = assertThrows(ProductException.class, () ->
                productService.createProduct(createRequest, mainImages, detailImages, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 등록 실패 - 스토어 없음")
    void createProduct_Fail_StoreNotFound() {
        // given
        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(storeRepository.findBySellerId(1L)).willReturn(Optional.empty());

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class, () ->
                        productService.createProduct(createRequest, mainImages, detailImages, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.STORE_NOT_FOUND);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 등록 실패 - 카테고리 없음")
    void createProduct_Fail_CategoryNotFound() {
        // given
        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(storeRepository.findBySellerId(1L)).willReturn(Optional.of(store));
        given(categoryRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class, () ->
                        productService.createProduct(createRequest, mainImages, detailImages, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 수정 성공")
    void updateProduct_Success() {
        // given
        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(1L)).willReturn(Optional.of(product));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        // when
        productService.updateProduct(1L, updateRequest, mainImages, detailImages, userDetails);

        // then
        verify(productRepository, times(1)).save(product);
        verify(productImageService, times(1)).replaceImages(product, mainImages, ImageType.MAIN);
        verify(productImageService, times(1)).replaceImages(product, detailImages, ImageType.DETAIL);
    }

    @Test
    @DisplayName("상품 수정 성공 - 이미지 없이")
    void updateProduct_Success_WithoutImages() {
        // given
        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(1L)).willReturn(Optional.of(product));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        // when
        productService.updateProduct(1L, updateRequest, null, null, userDetails);

        // then
        verify(productRepository, times(1)).save(product);
        verify(productImageService, never()).replaceImages(any(), any(), any());
    }

    @Test
    @DisplayName("상품 수정 실패 - 상품 없음")
    void updateProduct_Fail_ProductNotFound() {
        // given
        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(1L)).willReturn(Optional.empty());

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class, () ->
                        productService.updateProduct(1L, updateRequest, mainImages, detailImages, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 수정 실패 - 소유권 없음")
    void updateProduct_Fail_NotOwner() {
        // given
        Store otherStore = Store.builder()
                .storeName("다른 스토어")
                .sellerId(2L)
                .build();
        ReflectionTestUtils.setField(otherStore, "storeId", 2L);

        Product otherProduct = Product.builder()
                .name("다른 상품")
                .price(new BigDecimal("30000"))
                .store(otherStore)
                .category(category)
                .build();
        ReflectionTestUtils.setField(otherProduct, "productId", 2L);

        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(2L)).willReturn(Optional.of(otherProduct));

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class, () ->
                        productService.updateProduct(2L, updateRequest, mainImages, detailImages, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.STORE_FORBIDDEN);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void deleteProduct_Success() {
        // given
        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(1L)).willReturn(Optional.of(product));

        // when
        productService.deleteProduct(1L, userDetails);

        // then
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("상품 삭제 실패 - 상품 없음")
    void deleteProduct_Fail_ProductNotFound() {
        // given
        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(1L)).willReturn(Optional.empty());

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class, () ->
                        productService.deleteProduct(1L, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        verify(productRepository, never()).delete(any(Product.class));
    }
}