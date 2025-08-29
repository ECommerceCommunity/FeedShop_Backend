package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
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
@DisplayName("ProductOptionDeleteService 테스트")
class ProductOptionDeleteServiceTest {

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private ProductOptionHelper productOptionHelper;

    @InjectMocks
    private ProductOptionDeleteService productOptionDeleteService;

    private User sellerUser;
    private ProductOption testProductOption;

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

        Product testProduct = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("10000"))
                .store(testStore)
                .build();

        testProductOption = new ProductOption(Gender.MEN, Size.SIZE_250, Color.BLACK, 10, testProduct);
        ReflectionTestUtils.setField(testProductOption, "optionId", 1L);
    }

    @Test
    @DisplayName("상품 옵션 삭제 성공")
    void deleteProductOption_Success() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOption(1L)).willReturn(testProductOption);

        // when
        productOptionDeleteService.deleteProductOption(1L, "seller");

        // then
        verify(productOptionHelper).getCurrentUser("seller");
        verify(productOptionHelper).validateSellerRole(sellerUser);
        verify(productOptionHelper).getProductOption(1L);
        verify(productOptionHelper).validateSellerPermission(sellerUser, testProductOption);
        verify(productOptionHelper).validateNotOrderedOption(testProductOption);
        verify(productOptionRepository).delete(testProductOption);
    }

    @Test
    @DisplayName("상품 옵션 삭제 실패 - 사용자 없음")
    void deleteProductOption_Fail_UserNotFound() {
        // given
        given(productOptionHelper.getCurrentUser("nonexistent"))
                .willThrow(new ProductException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> productOptionDeleteService.deleteProductOption(1L, "nonexistent"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(productOptionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("상품 옵션 삭제 실패 - 판매자 권한 없음")
    void deleteProductOption_Fail_NotSeller() {
        // given
        User normalUser = new User("user", "password", "user@test.com", UserRole.USER);
        given(productOptionHelper.getCurrentUser("user")).willReturn(normalUser);
        doThrow(new ProductException(ErrorCode.FORBIDDEN))
                .when(productOptionHelper).validateSellerRole(normalUser);

        // when & then
        assertThatThrownBy(() -> productOptionDeleteService.deleteProductOption(1L, "user"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

        verify(productOptionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("상품 옵션 삭제 실패 - 옵션 없음")
    void deleteProductOption_Fail_OptionNotFound() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOption(999L))
                .willThrow(new ProductException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> productOptionDeleteService.deleteProductOption(999L, "seller"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_OPTION_NOT_FOUND);

        verify(productOptionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("상품 옵션 삭제 실패 - 주문 내역 존재")
    void deleteProductOption_Fail_OrderExists() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOption(1L)).willReturn(testProductOption);
        doThrow(new ProductException(ErrorCode.PRODUCT_IN_ORDER))
                .when(productOptionHelper).validateNotOrderedOption(testProductOption);

        // when & then
        assertThatThrownBy(() -> productOptionDeleteService.deleteProductOption(1L, "seller"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_IN_ORDER);

        verify(productOptionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("상품 옵션 삭제 메서드 호출 순서 검증")
    void deleteProductOption_CallOrderVerification() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOption(1L)).willReturn(testProductOption);

        // when
        productOptionDeleteService.deleteProductOption(1L, "seller");

        // then
        var inOrder = inOrder(productOptionHelper, productOptionRepository);
        inOrder.verify(productOptionHelper).getCurrentUser("seller");
        inOrder.verify(productOptionHelper).validateSellerRole(sellerUser);
        inOrder.verify(productOptionHelper).getProductOption(1L);
        inOrder.verify(productOptionHelper).validateSellerPermission(sellerUser, testProductOption);
        inOrder.verify(productOptionHelper).validateNotOrderedOption(testProductOption);
        inOrder.verify(productOptionRepository).delete(testProductOption);
    }
}