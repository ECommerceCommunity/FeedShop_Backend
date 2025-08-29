package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.exception.ProductException;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductDeleteService 테스트")
class ProductDeleteServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductHelper productHelper;

    @InjectMocks
    private ProductDeleteService productDeleteService;

    private User sellerUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        sellerUser = new User("seller", "password", "seller@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(sellerUser, "id", 1L);

        Store testStore = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(testStore, "storeId", 1L);

        testProduct = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("10000"))
                .store(testStore)
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void deleteProduct_Success() {
        // given
        given(productHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productHelper.getProductOwnership(1L, 1L)).willReturn(testProduct);

        // when
        productDeleteService.deleteProduct(1L, "seller");

        // then
        verify(productHelper).getCurrentUser("seller");
        verify(productHelper).validateSellerRole(sellerUser);
        verify(productHelper).getProductOwnership(1L, 1L);
        verify(productHelper).validateProductNotInOrder(testProduct);
    }

    @Test
    @DisplayName("상품 삭제 실패 - 사용자 없음")
    void deleteProduct_Fail_UserNotFound() {
        // given
        given(productHelper.getCurrentUser("nonexistent"))
                .willThrow(new ProductException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> productDeleteService.deleteProduct(1L, "nonexistent"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(productRepository, never()).delete(any());
    }

    @Test
    @DisplayName("상품 삭제 실패 - 판매자 권한 없음")
    void deleteProduct_Fail_NotSeller() {
        // given
        User normalUser = new User("user", "password", "user@test.com", UserRole.USER);
        given(productHelper.getCurrentUser("user")).willReturn(normalUser);
        doThrow(new ProductException(ErrorCode.FORBIDDEN))
                .when(productHelper).validateSellerRole(normalUser);

        // when & then
        assertThatThrownBy(() -> productDeleteService.deleteProduct(1L, "user"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

        verify(productRepository, never()).delete(any());
    }

    @Test
    @DisplayName("상품 삭제 실패 - 주문 내역 존재")
    void deleteProduct_Fail_OrderExists() {
        // given
        given(productHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productHelper.getProductOwnership(1L, 1L)).willReturn(testProduct);
        doThrow(new ProductException(ErrorCode.PRODUCT_IN_ORDER))
                .when(productHelper).validateProductNotInOrder(testProduct);

        // when & then
        assertThatThrownBy(() -> productDeleteService.deleteProduct(1L, "seller"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_IN_ORDER);

        verify(productRepository, never()).delete(any());
    }
}