package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.request.ProductCreateRequest;
import com.cMall.feedShop.product.application.dto.response.ProductCreateResponse;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCreateService 테스트")
class ProductCreateServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductHelper productHelper;

    @Mock
    private MultipartFile mainImage;

    @Mock
    private MultipartFile detailImage;

    @InjectMocks
    private ProductCreateService productCreateService;

    private User sellerUser;
    private Store testStore;
    private Category testCategory;
    private ProductCreateRequest createRequest;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        sellerUser = new User("seller", "password", "seller@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(sellerUser, "id", 1L);

        testStore = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(testStore, "storeId", 1L);

        testCategory = new Category(null, "테스트 카테고리");
        ReflectionTestUtils.setField(testCategory, "categoryId", 1L);

        testProduct = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("10000"))
                .store(testStore)
                .category(testCategory)
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);

        createRequest = new ProductCreateRequest();
        ReflectionTestUtils.setField(createRequest, "name", "테스트 상품");
        ReflectionTestUtils.setField(createRequest, "price", new BigDecimal("10000"));
        ReflectionTestUtils.setField(createRequest, "categoryId", 1L);
        ReflectionTestUtils.setField(createRequest, "description", "테스트 설명");
    }

    @Test
    @DisplayName("상품 생성 성공")
    void createProduct_Success() {
        // given
        List<MultipartFile> mainImages = Arrays.asList(mainImage);
        List<MultipartFile> detailImages = Arrays.asList(detailImage);

        given(productHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productHelper.getUserStore(1L)).willReturn(testStore);
        given(productHelper.getCategory(1L)).willReturn(testCategory);
        given(productRepository.save(any(Product.class))).willReturn(testProduct);

        // when
        ProductCreateResponse result = productCreateService.createProduct(createRequest, mainImages, detailImages, "seller");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);

        verify(productHelper).getCurrentUser("seller");
        verify(productHelper).validateSellerRole(sellerUser);
        verify(productHelper).getUserStore(1L);
        verify(productHelper).getCategory(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 생성 실패 - 사용자 없음")
    void createProduct_Fail_UserNotFound() {
        // given
        given(productHelper.getCurrentUser("nonexistent"))
                .willThrow(new ProductException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> productCreateService.createProduct(createRequest, null, null, "nonexistent"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 생성 실패 - 판매자 권한 없음")
    void createProduct_Fail_NotSeller() {
        // given
        User normalUser = new User("user", "password", "user@test.com", UserRole.USER);
        given(productHelper.getCurrentUser("user")).willReturn(normalUser);
        doThrow(new ProductException(ErrorCode.FORBIDDEN))
                .when(productHelper).validateSellerRole(normalUser);

        // when & then
        assertThatThrownBy(() -> productCreateService.createProduct(createRequest, null, null, "user"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

        verify(productRepository, never()).save(any());
    }
}