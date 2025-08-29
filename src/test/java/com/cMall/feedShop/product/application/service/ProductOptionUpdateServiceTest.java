package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.request.ProductOptionUpdateRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductOptionUpdateService 테스트")
class ProductOptionUpdateServiceTest {

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private ProductOptionHelper productOptionHelper;

    @InjectMocks
    private ProductOptionUpdateService productOptionUpdateService;

    private User sellerUser;
    private ProductOption testProductOption;
    private ProductOptionUpdateRequest updateRequest;

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

        testProductOption = new ProductOption(Gender.MEN, Size.SIZE_230, Color.BLACK, 10, testProduct);
        ReflectionTestUtils.setField(testProductOption, "optionId", 1L);

        updateRequest = new ProductOptionUpdateRequest();
    }

    @Test
    @DisplayName("상품 옵션 수정 성공")
    void updateProductOption_Success() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOption(1L)).willReturn(testProductOption);
        given(productOptionRepository.save(any(ProductOption.class))).willReturn(testProductOption);

        // when
        productOptionUpdateService.updateProductOption(1L, updateRequest, "seller");

        // then
        verify(productOptionHelper).getCurrentUser("seller");
        verify(productOptionHelper).validateSellerRole(sellerUser);
        verify(productOptionHelper).getProductOption(1L);
        verify(productOptionHelper).validateSellerPermission(sellerUser, testProductOption);
        verify(productOptionHelper).updateOptionInfo(testProductOption, updateRequest);
        verify(productOptionRepository).save(testProductOption);
    }

    @Test
    @DisplayName("상품 옵션 수정 실패 - 사용자 없음")
    void updateProductOption_Fail_UserNotFound() {
        // given
        given(productOptionHelper.getCurrentUser("nonexistent"))
                .willThrow(new ProductException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> productOptionUpdateService.updateProductOption(1L, updateRequest, "nonexistent"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(productOptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 옵션 수정 실패 - 판매자 권한 없음")
    void updateProductOption_Fail_NotSeller() {
        // given
        User normalUser = new User("user", "password", "user@test.com", UserRole.USER);
        given(productOptionHelper.getCurrentUser("user")).willReturn(normalUser);
        doThrow(new ProductException(ErrorCode.FORBIDDEN))
                .when(productOptionHelper).validateSellerRole(normalUser);

        // when & then
        assertThatThrownBy(() -> productOptionUpdateService.updateProductOption(1L, updateRequest, "user"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

        verify(productOptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 옵션 수정 실패 - 옵션 없음")
    void updateProductOption_Fail_OptionNotFound() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOption(999L))
                .willThrow(new ProductException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> productOptionUpdateService.updateProductOption(999L, updateRequest, "seller"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_OPTION_NOT_FOUND);

        verify(productOptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 옵션 수정 실패 - 권한 없음")
    void updateProductOption_Fail_NoPermission() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOption(1L)).willReturn(testProductOption);
        doThrow(new ProductException(ErrorCode.FORBIDDEN))
                .when(productOptionHelper).validateSellerPermission(sellerUser, testProductOption);

        // when & then
        assertThatThrownBy(() -> productOptionUpdateService.updateProductOption(1L, updateRequest, "seller"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

        verify(productOptionRepository, never()).save(any());
    }
}